package com.do_something.do_something.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 기반 In-memory 캐시 설정.
 * <p>외부 가격 API 호출 비용을 줄이기 위해 10분 TTL로 캐싱한다.
 * 기존 Python 코드의 {@code CACHE_DURATION = 600} (초)와 동일한 정책이다.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** 주식/암호화폐 가격 캐시 */
    public static final String PRICES         = "prices";
    /** 환율 캐시 (USD/KRW, USDT/KRW) */
    public static final String EXCHANGE_RATES = "exchangeRates";

    @Bean
    public CacheManager cacheManager() {
        final CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.registerCustomCache(PRICES,
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .build());

        manager.registerCustomCache(EXCHANGE_RATES,
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(10)
                        .build());

        return manager;
    }
}
