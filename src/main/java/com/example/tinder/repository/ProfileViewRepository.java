package com.example.tinder.repository;

import com.example.tinder.dto.ProfileViewStatsProjection;
import com.example.tinder.entity.Swipe;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProfileViewRepository extends JpaRepository<Swipe, Long> {
    @Query("""
        SELECT COUNT(s)
        FROM Swipe s
        WHERE s.swiped.id = :userId
    """)
    Long countViewsByUserId(@Param("userId") Long userId);

    @Query(nativeQuery = true, value = """
        SELECT 
            TO_CHAR(s.created_at, :format) AS period,
            COUNT(DISTINCT s.swiper_id) AS count
        FROM swipe s
        WHERE s.swiped_id = :userId
        AND s.created_at >= :start 
        AND s.created_at < :end
        GROUP BY period
        ORDER BY period
    """)
    List<ProfileViewStatsProjection> getProfileViewStats(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("format") String format
    );

}
