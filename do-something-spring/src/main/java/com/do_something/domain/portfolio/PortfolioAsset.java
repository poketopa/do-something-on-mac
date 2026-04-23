package com.do_something.domain.portfolio;

import com.do_something.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 포트폴리오 자산 Entity.
 * <p>한 사용자는 최대 3개의 프리셋을 가질 수 있으며,
 * 각 프리셋에 여러 자산({@code PortfolioAsset})을 등록한다.</p>
 */
@Entity
@Table(
    name = "portfolio_assets",
    indexes = @Index(name = "idx_owner_preset", columnList = "owner_id, preset")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "symbol", "preset"})
@EqualsAndHashCode(of = "id")
public class PortfolioAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false, length = 20)
    private String screener;

    @Column(nullable = false, length = 20)
    private String exchange;

    @Column(nullable = false)
    private int preset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * 포트폴리오 자산 생성 팩토리 메서드.
     */
    public static PortfolioAsset create(
            final String symbol,
            final double quantity,
            final String screener,
            final String exchange,
            final int preset,
            final User owner
    ) {
        final PortfolioAsset asset = new PortfolioAsset();
        asset.symbol   = symbol.toUpperCase();
        asset.quantity = quantity;
        asset.screener = screener;
        asset.exchange = exchange;
        asset.preset   = preset;
        asset.owner    = owner;
        return asset;
    }
}
