package com.example.tinder.entity;


import com.example.tinder.enums.ReportReason;
import com.example.tinder.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
        name = "user_report",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"reporter_id", "reported_id"}
        ),
        indexes = {
                @Index(name = "idx_report_reporter_reported", columnList = "reporter_id, reported_id"),
                @Index(name = "idx_report_reported_reporter", columnList = "reported_id, reporter_id")
        }
)
public class Report extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reportReason;

    @Column(length = 500)
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(length = 500)
    private String adminComment;

    private LocalDateTime reviewedAt;
}
