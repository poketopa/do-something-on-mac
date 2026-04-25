package com.do_something.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

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
