package com.example.tinder.entity;

import com.example.tinder.enums.SwipeStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "swipe",
        indexes = {
                @Index(name = "idx_swipe_swiper_swiped", columnList = "swiper_id, swiped_id")
        }
)

public class Swipe extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiper_id", nullable = false)
    private User swiper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiped_id", nullable = false)
    private User swiped;

    @Enumerated(EnumType.STRING)
    private SwipeStatus swipe;
}
