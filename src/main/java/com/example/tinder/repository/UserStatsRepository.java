package com.example.tinder.repository;

import com.example.tinder.dto.AllUserStatsProjection;
import com.example.tinder.dto.UserStatsProjection;
import com.example.tinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserStatsRepository extends JpaRepository<User,Long> {
    /*
   dashboard uchun user malumotlarini olish
    */


    @Query(nativeQuery = true, value = """
    SELECT 
        (SELECT COUNT(*) FROM users) as totalUsers,
        (SELECT COUNT(*) FROM users WHERE subscription_type = :gold) as goldUsers,
        (SELECT COUNT(*) FROM users WHERE is_approved = true) as approvedUsers
    """)
    AllUserStatsProjection getAllUserStatsNative(@Param("gold") String gold);


    @Query(nativeQuery = true, value = """
                SELECT 
                    TO_CHAR(u.created_at, :format) AS period,
                    COUNT(*) AS totalUsers,
                    COUNT(*) FILTER (WHERE u.subscription_type = :gold) AS goldUsers,
                    COUNT(*) FILTER (WHERE u.is_approved = true) AS approvedUsers
                FROM users u
                WHERE u.created_at >= :start AND u.created_at < :end
                GROUP BY period
                ORDER BY period
            """)
    List<UserStatsProjection> getUserStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("format") String format,
            @Param("gold") String gold
    );
}
