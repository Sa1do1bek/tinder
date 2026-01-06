package com.example.tinder.repository;

import com.example.tinder.entity.SuperLikePurchase;
import com.example.tinder.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuperLikePurchaseRepository extends JpaRepository<SuperLikePurchase, Long> {
    
    @Query("""
        SELECT p FROM SuperLikePurchase p
        WHERE p.user.id = :userId
        AND p.visibility = true
        ORDER BY p.createdAt DESC
    """)
    Page<SuperLikePurchase> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("""
        SELECT p FROM SuperLikePurchase p
        WHERE p.user.id = :userId
        AND p.status = :status
        AND p.visibility = true
        ORDER BY p.createdAt DESC
    """)
    List<SuperLikePurchase> findByUserIdAndStatus(
            @Param("userId") Long userId, 
            @Param("status") TransactionStatus status);
    
    @Query("""
        SELECT COUNT(p) FROM SuperLikePurchase p
        WHERE p.status = 'COMPLETED'
        AND p.createdAt >= :startDate
        AND p.visibility = true
    """)
    Long countSuccessfulPurchasesSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("""
        SELECT COALESCE(SUM(p.amountPaid), 0.0) FROM SuperLikePurchase p
        WHERE p.status = 'COMPLETED'
        AND p.createdAt >= :startDate
        AND p.visibility = true
    """)
    Double calculateTotalRevenueSince(@Param("startDate") LocalDateTime startDate);
    
    Optional<SuperLikePurchase> findByTransactionId(String transactionId);
}