package com.do_something.do_something.domain.portfolio;

import com.do_something.do_something.dto.portfolio.PortfolioResponse;
import com.do_something.do_something.dto.portfolio.PortfolioUpdateRequest;
import com.do_something.do_something.dto.portfolio.PortfolioValueResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 포트폴리오 REST Controller.
 */
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Validated
public class PortfolioController {

    private final PortfolioService portfolioService;

    /** GET /api/portfolio?preset=1 — 자산 목록 조회 */
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolio(
            @AuthenticationPrincipal final UserDetails user,
            @RequestParam(defaultValue = "1") @Min(1) @Max(3) final int preset
    ) {
        return ResponseEntity.ok(portfolioService.getPortfolio(user.getUsername(), preset));
    }

    /** POST /api/portfolio?preset=1 — 자산 목록 저장 (204 No Content) */
    @PostMapping
    public ResponseEntity<Void> savePortfolio(
            @AuthenticationPrincipal final UserDetails user,
            @RequestParam(defaultValue = "1") @Min(1) @Max(3) final int preset,
            @RequestBody @Valid final PortfolioUpdateRequest request
    ) {
        portfolioService.savePortfolio(user.getUsername(), preset, request);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/portfolio/value?preset=1 — 포트폴리오 총 평가액 계산 */
    @GetMapping("/value")
    public ResponseEntity<PortfolioValueResponse> getPortfolioValue(
            @AuthenticationPrincipal final UserDetails user,
            @RequestParam(defaultValue = "1") @Min(1) @Max(3) final int preset
    ) {
        return ResponseEntity.ok(portfolioService.calculateValue(user.getUsername(), preset));
    }
}
