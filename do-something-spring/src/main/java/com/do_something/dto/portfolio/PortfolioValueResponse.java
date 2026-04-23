package com.do_something.dto.portfolio;

import java.util.List;

/**
 * 포트폴리오 총 평가액 응답 DTO.
 */
public record PortfolioValueResponse(
        boolean success,
        List<AssetValueDto> portfolio,
        double totalUsd,
        double totalKrw,
        double usdKrwRate,
        double usdtKrwRate
) {}
