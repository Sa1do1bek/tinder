package com.example.tinder.repository;

import com.example.tinder.dto.payment.PaymentTransactionProjection;
import com.example.tinder.entity.PaymentTransaction;
import com.example.tinder.enums.payment.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByIdAndPaymentMethod(Long id, PaymentMethod paymentMethod);

    Optional<PaymentTransaction> findByPaymentMethodAndProviderTransactionId(PaymentMethod paymentMethod, String providerTransactionId);

    List<PaymentTransaction> findAllByPaymentMethodAndCreateTimeIsBetween(PaymentMethod paymentMethod, Long from, Long to);


    @Query("""
            SELECT p FROM PaymentTransaction p
            WHERE p.paymentMethod = :method
            AND  p.createdAt >= :start
            AND p.createdAt <= :end
            """)
    Page<PaymentTransactionProjection> findAllProjected(
            PaymentMethod method,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );


    @Query("""
                SELECT p FROM PaymentTransaction p
                WHERE p.createdAt >= :start
            """)
    Page<PaymentTransactionProjection> findAllProjected(
            LocalDateTime start,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM PaymentTransaction p
                WHERE p.createdAt <= :end
            """)
    Page<PaymentTransactionProjection> findAllProjectedWithEndDate(
            LocalDateTime end,
            Pageable pageable
    );


    @Query("""
                SELECT p FROM PaymentTransaction p
                WHERE p.createdAt >= :start
                AND p.createdAt <= :end
            """)
    Page<PaymentTransactionProjection> findAllProjected(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM PaymentTransaction p
                WHERE p.paymentMethod = :method
            """)
    Page<PaymentTransactionProjection> findAllProjected(
            PaymentMethod method,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM PaymentTransaction p
            """)
    Page<PaymentTransactionProjection> findAllProjected(Pageable pageable);
}
