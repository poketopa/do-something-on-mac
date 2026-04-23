package com.do_something.price;

import com.do_something.config.CacheConfig;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 외부 가격 API 호출 및 캐싱 서비스.
 *
 * <ul>
 *   <li>주식 — Yahoo Finance v8 (비공식 공개 API)</li>
 *   <li>암호화폐 / 환율 — TradingView Scanner API (비공식 공개 API)</li>
 * </ul>
 *
 * <p>두 API 모두 공식 키가 불필요하지만, 서비스 정책 변경으로 언제든 차단될 수 있다.
 * 안정적인 서비스 전환 시 Alpha Vantage, CoinGecko Pro 등으로 교체를 권장한다.</p>
 */
@Slf4j
@Service
public class PriceService {

    private static final String TV_SCANNER_URL  =
            "https://scanner.tradingview.com/symbol?symbol={exchange}:{symbol}&fields=close&no_404=true";
    private static final String YAHOO_CHART_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}?interval=1d&range=1d";

    private final RestClient restClient;

    public PriceService() {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .build();
    }

    // ------------------------------------------------------------------ //
    //  환율 (10분 캐시)
    // ------------------------------------------------------------------ //

    /** USD/KRW 환율. TradingView FX_IDC 기준. */
    @Cacheable(value = CacheConfig.EXCHANGE_RATES, key = "'USD_KRW'")
    public double getUsdKrwRate() {
        return fetchTradingViewPrice("FX_IDC", "USDKRW");
    }

    /** USDT/KRW 환율. Bithumb 기준 (김치 프리미엄 반영). */
    @Cacheable(value = CacheConfig.EXCHANGE_RATES, key = "'USDT_KRW'")
    public double getUsdtKrwRate() {
        return fetchTradingViewPrice("BITHUMB", "USDTKRW");
    }

    // ------------------------------------------------------------------ //
    //  암호화폐 가격 (10분 캐시)
    // ------------------------------------------------------------------ //

    /**
     * TradingView 기준 암호화폐 USD 가격.
     *
     * @param symbol   예) BTCUSDT
     * @param exchange 예) BINANCE
     */
    @Cacheable(value = CacheConfig.PRICES, key = "#exchange + ':' + #symbol")
    public double getCryptoPrice(final String symbol, final String exchange) {
        return fetchTradingViewPrice(exchange, symbol);
    }

    // ------------------------------------------------------------------ //
    //  주식 가격 (10분 캐시)
    // ------------------------------------------------------------------ //

    /**
     * Yahoo Finance 기준 종목 USD 가격.
     * 실패 시 TradingView NASDAQ fallback.
     *
     * @param symbol 예) AAPL, TSLA, 005930.KS (한국주식)
     */
    @Cacheable(value = CacheConfig.PRICES, key = "#symbol")
    public double getStockPrice(final String symbol) {
        try {
            return fetchYahooPrice(symbol);
        } catch (Exception e) {
            log.warn("Yahoo Finance 실패 [{}], TradingView fallback 시도: {}", symbol, e.getMessage());
            return fetchTradingViewPrice("NASDAQ", symbol);
        }
    }

    // ------------------------------------------------------------------ //
    //  내부 HTTP 호출
    // ------------------------------------------------------------------ //

    private double fetchTradingViewPrice(final String exchange, final String symbol) {
        final JsonNode body = restClient.get()
                .uri(TV_SCANNER_URL, exchange, symbol)
                .retrieve()
                .body(JsonNode.class);

        if (body == null || !body.has("close")) {
            log.error("TradingView 가격 조회 실패: {}:{}", exchange, symbol);
            return 0.0;
        }
        return body.get("close").asDouble();
    }

    private double fetchYahooPrice(final String symbol) {
        final JsonNode root = restClient.get()
                .uri(YAHOO_CHART_URL, symbol)
                .retrieve()
                .body(JsonNode.class);

        final JsonNode meta = root
                .path("chart")
                .path("result")
                .path(0)
                .path("meta");

        // regularMarketPrice 우선, 없으면 chartPreviousClose 사용
        if (meta.has("regularMarketPrice")) {
            return meta.get("regularMarketPrice").asDouble();
        }
        return meta.path("chartPreviousClose").asDouble();
    }
}
