package com.example.tinder.repository;

import com.example.tinder.entity.PlatformRevenue;
import com.example.tinder.enums.RevenueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
        WHERE r.purchase.id = :purchaseId
        AND r.type = 'INCOME'
        AND r.visibility = true
    """)
    Optional<PlatformRevenue> findByPurchaseId(@Param("purchaseId") Long purchaseId);


    @Query("""
        SELECT r FROM PlatformRevenue r
        WHERE r.visibility = true
        ORDER BY r.createdAt DESC
    """)
    Page<PlatformRevenue> findAllRevenues(Pageable pageable);


    @Query("""
        SELECT r FROM PlatformRevenue r
        WHERE r.type = 'EXPENSE'
        AND r.walletTransaction.user.id = :userId
        AND r.visibility = true
        ORDER BY r.createdAt DESC
    """)
    Page<PlatformRevenue> findExpensesByUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("""
        SELECT COALESCE(SUM(r.platformAmount), 0) FROM PlatformRevenue r
        WHERE r.type = 'INCOME'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    BigDecimal calculateTotalIncome(@Param("startDate") LocalDateTime startDate);


    @Query("""
        SELECT COALESCE(SUM(r.platformAmount), 0) FROM PlatformRevenue r
        WHERE r.type = 'EXPENSE'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    BigDecimal calculateTotalExpense(@Param("startDate") LocalDateTime startDate);


    @Query("""
        SELECT COALESCE(SUM(r.totalAmount), 0) FROM PlatformRevenue r
        WHERE r.type = 'EXPENSE'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    BigDecimal calculateTotalWithdrawals(@Param("startDate") LocalDateTime startDate);


    @Query("""
        SELECT COALESCE(SUM(r.totalAmount), 0) FROM PlatformRevenue r
        WHERE r.type = 'INCOME'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    BigDecimal calculateGrossIncome(@Param("startDate") LocalDateTime startDate);


    @Query("""
        SELECT r FROM PlatformRevenue r
        WHERE (:type IS NULL OR r.type = :type)
        AND r.visibility = true
        ORDER BY r.createdAt DESC
    """)
    Page<PlatformRevenue> findAllByType(@Param("type") RevenueType type, Pageable pageable);



    @Query("""
        SELECT r FROM PlatformRevenue r
        WHERE r.walletTransaction.id = :transactionId
        AND r.type = 'EXPENSE'
        AND r.visibility = true
    """)
    Optional<PlatformRevenue> findByWalletTransactionId(@Param("transactionId") Long transactionId);


    @Query("""
        SELECT r FROM PlatformRevenue r
        WHERE r.type = :type
        AND r.createdAt BETWEEN :startDate AND :endDate
        AND r.visibility = true
        ORDER BY r.createdAt DESC
    """)
    Page<PlatformRevenue> findByTypeAndDateRange(
            @Param("type") RevenueType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);


    @Query("""
        SELECT COUNT(r) FROM PlatformRevenue r
        WHERE r.type = 'INCOME'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    Long countIncomeTransactions(@Param("startDate") LocalDateTime startDate);


    @Query("""
        SELECT COUNT(r) FROM PlatformRevenue r
        WHERE r.type = 'EXPENSE'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    Long countExpenseTransactions(@Param("startDate") LocalDateTime startDate);


    @Query("""
        SELECT AVG(r.totalAmount) FROM PlatformRevenue r
        WHERE r.type = 'INCOME'
        AND r.createdAt >= :startDate
        AND r.visibility = true
    """)
    BigDecimal calculateAveragePurchaseAmount(@Param("startDate") LocalDateTime startDate);



}