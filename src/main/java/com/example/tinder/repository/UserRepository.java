package com.example.tinder.repository;

import com.example.tinder.entity.User;
import com.example.tinder.enums.Gender;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "delete from user_roles as us where us.user_id=?1 and us.role_id=?2", nativeQuery = true)
    void deleteRoleFromUser(Long userId, Long roleId);

    @Query(value = "select * from users as u where u.status='ACTIVE'", nativeQuery = true)
    Page<User> getAllActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(String username);

    @Query("""
                SELECT u FROM User u 
                WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
                AND (:gender IS NULL OR u.gender = :gender)
            """)
    List<User> searchUsers(
            @Param("username") String username,
            @Param("gender") Gender gender
    );


    boolean existsByEmail(String email);

    @Query("""
                SELECT u FROM User u
                WHERE u.status = 'ACTIVE'
                  AND u.id <> :userId
                  AND u.id NOT IN (
                      SELECT s.swiped.id FROM Swipe s
                      WHERE s.swiper.id = :userId
                  )
            """)
    Page<User> getAllUnswipedActiveUsers(@Param("userId") Long userId, Pageable pageable);

    Optional<User> findByUsernameAndActive(String username, boolean active);

    boolean existsByEmailAndActiveTrue(@NotBlank(message = "email cannot be null or empty") @Email String email);

    boolean existsByUsernameAndActiveTrue(String username);

    @Query(value = """
    SELECT DISTINCT u.*
    FROM users u
    JOIN preference p ON p.user_preference = :currentUserId

    -- Userning o'zidagi interestlar bilan compare qilish uchun
    JOIN users_interest ui ON ui.user_interest = u.id

    WHERE 
        u.id != :currentUserId
        AND u.active = true

        -- Gender preference
        AND (
            p.preferred_gender IS NULL 
            OR u.gender = p.preferred_gender
        )

        -- Age preference
        AND u.age BETWEEN p.min_age AND p.max_age

        -- Interest match (kamida bitta mos bo'lishi shart)
        AND EXISTS (
            SELECT 1 
            FROM users_interest cui
            WHERE cui.user_interest = u.id
            AND cui.name IN (
                SELECT name 
                FROM users_interest
                WHERE preference_id = p.id
            )
        )
    """, nativeQuery = true)
    List<User> findRecommendations(
            @Param("currentUserId") Long currentUserId);

}
