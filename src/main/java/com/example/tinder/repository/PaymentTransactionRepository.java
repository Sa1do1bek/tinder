package com.example.tinder.repository;

import com.example.tinder.entity.PaymentTransaction;
import com.example.tinder.enums.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByIdAndPaymentMethod(Long id, PaymentMethod paymentMethod);

    Optional<PaymentTransaction> findByPaymentMethodAndProviderTransactionId(PaymentMethod paymentMethod, String providerTransactionId);

    List<PaymentTransaction> findAllByPaymentMethodAndCreateTimeIsBetween(PaymentMethod paymentMethod, Long from, Long to);
}
