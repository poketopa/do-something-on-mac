package com.do_something.domain.portfolio;

import com.do_something.domain.user.User;
import com.do_something.domain.user.UserRepository;
import com.do_something.do_something.dto.portfolio.*;
import com.do_something.dto.portfolio.AssetValueDto;
import com.do_something.dto.portfolio.PortfolioResponse;
import com.do_something.dto.portfolio.PortfolioUpdateRequest;
import com.do_something.dto.portfolio.PortfolioValueResponse;
import com.do_something.exception.UserNotFoundException;
import com.do_something.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 포트폴리오 관리 비즈니스 로직.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository      userRepository;
    private final PriceService        priceService;

    /**
     * 특정 프리셋의 자산 목록 조회.
     */
    public List<PortfolioResponse> getPortfolio(final String username, final int preset) {
        final User user = findUser(username);
        return portfolioRepository.findAllByOwnerIdAndPreset(user.getId(), preset).stream()
                .map(PortfolioResponse::from)
                .toList();
    }

    /**
     * 특정 프리셋의 자산 목록 저장 (전체 교체 방식 — upsert 대신 delete-insert).
     * <p>기존 Python 코드와 동일한 전략: 해당 preset 자산을 모두 지운 뒤 새로 저장.</p>
     */
    @Transactional
    public void savePortfolio(final String username, final int preset, final PortfolioUpdateRequest request) {
        final User user = findUser(username);

        portfolioRepository.deleteAllByOwnerIdAndPreset(user.getId(), preset);

        final List<PortfolioAsset> assets = request.assets().stream()
                .map(dto -> PortfolioAsset.create(
                        dto.symbol(), dto.quantity(), dto.screener(), dto.exchange(), preset, user))
                .toList();

        portfolioRepository.saveAll(assets);
    }

    /**
     * 포트폴리오 총 평가액 계산.
     * <p>외부 가격 API 호출은 {@link PriceService}에 위임하며, Caffeine 캐시로 중복 호출을 방지한다.</p>
     */
    public PortfolioValueResponse calculateValue(final String username, final int preset) {
        final User user = findUser(username);
        final List<PortfolioAsset> assets = portfolioRepository.findAllByOwnerIdAndPreset(user.getId(), preset);

        if (assets.isEmpty()) {
            return new PortfolioValueResponse(true, List.of(), 0, 0, 0, 0);
        }

        final double usdKrwRate  = priceService.getUsdKrwRate();
        final double usdtKrwRate = priceService.getUsdtKrwRate();

        final List<AssetValueDto> assetValues = assets.stream()
                .map(asset -> resolveAssetValue(asset, usdKrwRate, usdtKrwRate))
                .toList();

        final double totalUsd = assetValues.stream().mapToDouble(AssetValueDto::totalUsd).sum();
        final double totalKrw = assetValues.stream().mapToDouble(AssetValueDto::totalKrw).sum();

        return new PortfolioValueResponse(true, assetValues, totalUsd, totalKrw, usdKrwRate, usdtKrwRate);
    }

    // --- private helpers ---

    private AssetValueDto resolveAssetValue(
            final PortfolioAsset asset,
            final double usdKrwRate,
            final double usdtKrwRate
    ) {
        final double usdPrice;
        final double krwPrice;

        if ("cash".equalsIgnoreCase(asset.getScreener())) {
            switch (asset.getSymbol().toUpperCase()) {
                case "KRW"  -> { usdPrice = asset.getQuantity() / usdKrwRate; krwPrice = asset.getQuantity(); }
                case "USDT" -> { usdPrice = asset.getQuantity(); krwPrice = asset.getQuantity() * usdtKrwRate; }
                default     -> { usdPrice = asset.getQuantity(); krwPrice = asset.getQuantity() * usdKrwRate; } // USD
            }
            return new AssetValueDto(
                    asset.getSymbol(), asset.getQuantity(), asset.getScreener(), asset.getExchange(),
                    usdPrice, krwPrice, usdPrice, krwPrice
            );
        }

        if ("crypto".equalsIgnoreCase(asset.getScreener())) {
            usdPrice = priceService.getCryptoPrice(asset.getSymbol(), asset.getExchange());
            krwPrice = usdPrice * usdtKrwRate;
        } else {
            usdPrice = priceService.getStockPrice(asset.getSymbol());
            krwPrice = usdPrice * usdKrwRate;
        }

        return new AssetValueDto(
                asset.getSymbol(), asset.getQuantity(), asset.getScreener(), asset.getExchange(),
                usdPrice, krwPrice,
                usdPrice * asset.getQuantity(),
                krwPrice * asset.getQuantity()
        );
    }

    private User findUser(final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
