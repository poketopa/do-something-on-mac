package com.do_something.dto.portfolio;

import com.do_something.domain.portfolio.PortfolioAsset;

/**
 * 포트폴리오 자산 조회 응답 DTO.
 */
public record PortfolioResponse(
        Long id,
        String symbol,
        double quantity,
        String screener,
        String exchange,
        int preset,
        Long ownerId
) {
    public static PortfolioResponse from(final PortfolioAsset asset) {
        return new PortfolioResponse(
                asset.getId(),
                asset.getSymbol(),
                asset.getQuantity(),
                asset.getScreener(),
                asset.getExchange(),
                asset.getPreset(),
                asset.getOwner().getId()
        );
    }
}
