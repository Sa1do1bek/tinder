package com.example.tinder.repository;

import com.example.tinder.dto.wallet.WalletTransactionProjection;
import com.example.tinder.entity.User;
import com.example.tinder.entity.WalletTransaction;
import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.enums.payment.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    @Query("""
        SELECT t FROM WalletTransaction t
        WHERE t.user.id = :userId
        AND t.visibility = true
        ORDER BY t.createdAt DESC
    """)
    Page<WalletTransaction> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<WalletTransaction> findByUserId(Long userId);
    
    @Query("""
        SELECT t FROM WalletTransaction t
        WHERE t.user.id = :userId
        AND t.type = :type
        AND t.visibility = true
        ORDER BY t.createdAt DESC
    """)
    Page<WalletTransaction> findByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            Pageable pageable);
    
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0.0) FROM WalletTransaction t
        WHERE t.user.id = :userId
        AND t.type = 'EARN'
        AND t.status = 'COMPLETED'
        AND t.visibility = true
    """)
    Double calculateTotalEarnings(@Param("userId") Long userId);
    
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0.0) FROM WalletTransaction t
        WHERE t.user.id = :userId
        AND t.type = 'WITHDRAW'
        AND t.status = 'COMPLETED'
        AND t.visibility = true
    """)
    Double calculateTotalWithdrawals(@Param("userId") Long userId);
    
    @Query("""
        SELECT t FROM WalletTransaction t
        WHERE t.user.id = :userId
        AND t.status = :status
        AND t.visibility = true
        ORDER BY t.createdAt DESC
    """)
    List<WalletTransaction> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status);

    @Query("SELECT wt FROM WalletTransaction wt " +
            "WHERE wt.user = :user " +
            "AND wt.balanceAfter = :balanceAfter " +
            "AND wt.status = :status " +
            "AND wt.paymentMethod = :paymentMethod " +
            "ORDER BY wt.createdAt DESC " +
            "LIMIT 1")
    Optional<WalletTransaction> findFirstByUserAndBalanceAfterAndStatusAndPaymentMethodOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("balanceAfter") BigDecimal balanceAfter,
            @Param("status") TransactionStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod
    );

    @Query("""
            SELECT w FROM WalletTransaction w
            WHERE w.paymentMethod = :method
            AND  w.createdAt >= :start
            AND w.createdAt <= :end
            """)
    Page<WalletTransactionProjection> findAllProjected(
            PaymentMethod method,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT w FROM WalletTransaction w
            WHERE w.createdAt >= :start
            """)
    Page<WalletTransactionProjection> findAllProjected(
            LocalDateTime start,
            Pageable pageable
    );

    @Query("""
            SELECT w FROM WalletTransaction w
            WHERE w.createdAt <= :end
            """)
    Page<WalletTransactionProjection> findAllProjectedWithEndDate(
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT w FROM WalletTransaction w
            WHERE w.createdAt >= :start
            AND w.createdAt <= :end
            """)
    Page<WalletTransactionProjection> findAllProjected(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );


    @Query("""
            SELECT w FROM WalletTransaction w
            WHERE w.paymentMethod = :method
            """)
    Page<WalletTransactionProjection> findAllProjected(
            PaymentMethod method,
            Pageable pageable
    );


    @Query("""
            SELECT w FROM WalletTransaction w  
             """)
    Page<WalletTransactionProjection> findAllProjected(Pageable pageable);
}