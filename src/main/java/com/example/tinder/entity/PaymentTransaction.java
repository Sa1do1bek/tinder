package com.example.tinder.entity;

import com.example.tinder.enums.payment.PaymeTransactionState;
import com.example.tinder.enums.payment.PaymentMethod;
import com.example.tinder.enums.payment.PaymentStatusForClick;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "payment_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_method_provider_transaction",
                        columnNames = {"payment_method", "provider_transaction_id"}
                )
        },
        indexes = {
                @Index(name = "idx_payment_method_provider_tx", columnList = "payment_method, provider_transaction_id"),
                @Index(name = "idx_payment_method_create_time", columnList = "payment_method, create_time")
        }
)
public class PaymentTransaction extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "provider_transaction_id", nullable = false)
    private String providerTransactionId;

    @Column(name = "merchant_transaction_id")
    private Long merchantTransactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "click_prepare_time")
    private LocalDateTime prepareTime;

    @Column(name = "click_complete_time")
    private LocalDateTime completeTime;

    @Column(name = "click_cancel_time")
    private LocalDateTime clickCancelTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "click_status")
    private PaymentStatusForClick clickStatus;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "perform_time")
    private Long performTime;

    @Column(name = "cancel_time")
    private Long cancelTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payme_state")
    private PaymeTransactionState state;

    private Integer reason;

    private Long walletTransactionId;

    public boolean isClickComplete() {
        return clickStatus == PaymentStatusForClick.COMPLETE;
    }

    public boolean isClickCancel() {
        return clickStatus == PaymentStatusForClick.CANCEL;
    }
}
