package com.do_something.dto.portfolio;

/**
 * 가격 계산 후 개별 자산의 평가 결과 DTO.
 */
public record AssetValueDto(
        String symbol,
        double quantity,
        String screener,
        String exchange,
        double usdPrice,
        double krwPrice,
        double totalUsd,
        double totalKrw
) {}
