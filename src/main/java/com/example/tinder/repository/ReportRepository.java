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
import java.util.List;


@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {




    @Query("""
        SELECT new com.example.tinder.dto.AllReportDto(
            reported.id,
            reported.username,
            reported.email,
            reported.profilePic,
            reported.phoneNumber,
            COUNT(r.id))
        FROM Report r
        JOIN r.reported reported
        WHERE r.reportStatus = :type
        GROUP BY 
            reported.id, 
            reported.username, 
            reported.email,
            reported.profilePic,
            reported.phoneNumber
        ORDER BY COUNT(r.id) DESC
""")
    Page<AllReportDto> findAllReport(ReportStatus type, Pageable pageable);



    Page<Report> findAllByReportedId(Long reportedUserId, Pageable pageable);


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


    @Query("""
    SELECT new com.example.tinder.dto.ReportAdminDTO(
        r.id,
        r.reporter.username,
        r.reported.username,
        r.reportReason,
        r.reportStatus,
        r.createdAt,
        r.reviewedBy.id,
        r.adminComment,
        r.reviewedAt
    )
    FROM Report r
""")
    Page<ReportAdminDTO> findAllReportsForAdmin(Pageable pageable);

    @Query("""
            SELECT r FROM Report r 
            LEFT JOIN FETCH r.reporter 
            LEFT JOIN FETCH r.reported 
            WHERE r.reported.id = :reportedId 
            ORDER BY r.createdAt DESC
            """)
    List<Report> findByReportedIdWithBothUsers(@Param("reportedId") Long reportedId);
}
