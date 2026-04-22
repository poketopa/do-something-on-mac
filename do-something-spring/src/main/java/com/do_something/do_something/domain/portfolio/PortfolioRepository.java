package com.do_something.do_something.domain.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * {@link PortfolioAsset} Entity의 Data Access Layer.
 */
public interface PortfolioRepository extends JpaRepository<PortfolioAsset, Long> {

    /**
     * 특정 사용자의 특정 프리셋 자산 목록 조회.
     */
    List<PortfolioAsset> findAllByOwnerIdAndPreset(Long ownerId, int preset);

    /**
     * 특정 사용자의 특정 프리셋 자산 전체 삭제.
     * <p>JPQL bulk delete를 사용해 SELECT → DELETE N+1 문제를 방지한다.</p>
     */
    @Modifying
    @Query("DELETE FROM PortfolioAsset pa WHERE pa.owner.id = :ownerId AND pa.preset = :preset")
    void deleteAllByOwnerIdAndPreset(@Param("ownerId") Long ownerId, @Param("preset") int preset);
}
