package com.example.tinder.repository;

import com.example.tinder.entity.PlatformRevenue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PlatformRevenueRepository extends JpaRepository<PlatformRevenue, Long> {
    
    @Query("""
        SELECT COALESCE(SUM(r.platformAmount), 0.0) FROM PlatformRevenue r
        WHERE r.createdAt >= :startDate
        AND r.visibility = true
    """)
    Double calculateTotalPlatformRevenue(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT COALESCE(SUM(r.userRewardAmount), 0.0) FROM PlatformRevenue r
        WHERE r.createdAt >= :startDate
        AND r.visibility = true
    """)
    Double calculateTotalUserRewards(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT r FROM PlatformRevenue r
        WHERE r.visibility = true
        ORDER BY r.createdAt DESC
    """)
    Page<PlatformRevenue> findAllRevenues(Pageable pageable);
    
    Optional<PlatformRevenue> findByPurchaseId(Long purchaseId);
}