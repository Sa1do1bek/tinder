package com.example.tinder.repository;

import com.example.tinder.dto.UserStatsProjection;
import com.example.tinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserStatsRepository extends JpaRepository<User,Long> {
    /*
   dashboard uchun user malumotlarini olish
    */

        @Query(nativeQuery = true, value = """
        SELECT 
            TO_CHAR(u.created_at, :format) AS period,
            COUNT(*) AS count
        FROM users u
        WHERE u.created_at >= :start AND u.created_at < :end
        AND u.visibility = true
        GROUP BY period
        ORDER BY period
        """)
        List<UserStatsProjection> getUserStats(
                @Param("start") LocalDate start,
                @Param("end") LocalDate end,
                @Param("format") String format
        );
}
