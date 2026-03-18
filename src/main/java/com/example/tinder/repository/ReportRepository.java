package com.example.tinder.repository;

import com.example.tinder.dto.AllReportDto;
import com.example.tinder.dto.ReportAdminDTO;
import com.example.tinder.entity.Report;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {


    @Query("""
            SELECT new com.example.tinder.dto.ReportAdminDTO(
                    r.id,
                    reporter.username,
                    reported.username,
                    r.reportReason,
                    r.reportStatus,
                    r.createdAt,
                    reviewer.id,
                    r.adminComment,
                    r.reviewedAt
                )
                FROM Report r
                JOIN r.reporter reporter
                JOIN r.reported reported
                LEFT JOIN r.reviewedBy reviewer
                WHERE reported.id = :reportedId
                AND (:status IS NULL OR r.reportStatus = :status)
            """)
    Page<ReportAdminDTO> findReports(
            @Param("reportedId") Long reportedId,
            @Param("status") ReportStatus status,
            Pageable pageable);

    @Query("""
            SELECT new com.example.tinder.dto.AllReportDto(
                reported.id,
                reported.username,
                COUNT(r.id))
                    FROM Report r
                    JOIN r.reported reported
                    WHERE r.reportStatus = :type
                    GROUP BY reported.id, reported.username
                    ORDER BY COUNT(r.id) DESC
            """)
    Page<AllReportDto> findAllReport(ReportStatus type, Pageable pageable);


    @Query("""
                select CASE WHEN COUNT(r) > 0 THEN true ELSE false END
                from Report r
                where r.reporter.id = :reporterId
                and r.reported.id = :reportedId
                and r.visibility = true
            """)
    boolean existsByReporterIdAndReportedId(Long reporterId, Long reportedId);

    boolean existsByReported_Id(Long id);

    @Modifying
    @Query("""
            update Report r
            SET r.reviewedBy = :user,
                r.reviewedAt = :reviewedAt,
                r.reportStatus = :reportStatus,
                r.adminComment = :adminComment
            where r.id = :id
            AND r.reportStatus = :pendingStatus
            AND r.reviewedBy is null
            """)
    int updateReviewedBy(@Param("id") Long id,
                         @Param("user") User user,
                         @Param("reviewedAt") LocalDateTime reviewedAt,
                         @Param("reportStatus") ReportStatus reportStatus,
                         @Param("adminComment") String adminComment,
                         @Param("pendingStatus") ReportStatus pendingStatus);

}
