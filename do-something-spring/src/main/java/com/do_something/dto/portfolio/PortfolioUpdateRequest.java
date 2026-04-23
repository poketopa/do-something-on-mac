package com.do_something.dto.portfolio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 포트폴리오 저장 요청 DTO.
 */
public record PortfolioUpdateRequest(
        @NotNull @Valid List<AssetDto> assets
) {}
