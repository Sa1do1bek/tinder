package com.example.tinder.entity;

import com.example.tinder.enums.PaymentStatus;
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
public class ClickTransaction extends BaseEntity {

    @Id
    private Long id;

    private Long userId;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "prepare_time")
    private LocalDateTime prepareTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public boolean isComplete() {
        return status == PaymentStatus.COMPLETE;
    }

    public boolean isCancel() {
        return status == PaymentStatus.CANCEL;
    }
}
