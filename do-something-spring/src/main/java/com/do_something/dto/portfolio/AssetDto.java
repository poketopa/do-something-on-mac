package com.do_something.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * 개별 자산 요청/응답 DTO.
 */
public record AssetDto(
        @NotBlank String symbol,
        @Positive double quantity,
        @NotBlank String screener,
        @NotBlank String exchange
) {}
