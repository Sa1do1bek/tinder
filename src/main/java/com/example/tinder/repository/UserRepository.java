package com.example.tinder.repository;

import com.example.tinder.entity.User;
import com.example.tinder.entity.UserInterest;
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

    @Query("""
            SELECT u FROM User u
            WHERE u.email = :email\s
            AND u.visibility = true
   \s""")
    Optional<User> findByEmail(@Param("email") String email);

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

    @Query(value = """
            SELECT u FROM User u
             WHERE u.status = 'ACTIVE'
               AND u.id <> :userId
               AND u.id NOT IN (
                   SELECT s.swiped.id FROM Swipe s
                   WHERE s.swiper.id = :userId
               )""",

           countQuery =
                """
                SELECT COUNT(u) FROM User u
                    WHERE u.status =:status
                    AND u.id<> :userId
                    AND u.id NOT
                    IN(SELECT s2.swiped.id FROM Swipe s2
                        WHERE s2.swiper.id=:userId
                       )
                """)
    Page<User> getAllUnswipedActiveUsers(@Param("userId") Long userId, Pageable pageable);

    Optional<User> findByUsernameAndActive(String username, boolean active);

    boolean existsByEmailAndActiveTrue(@NotBlank(message = "email cannot be null or empty") @Email String email);

    boolean existsByUsernameAndActiveTrue(String username);


    //:preferredGender = 'EVERYONE' OR


    @Query("""
    SELECT u FROM User u 
    WHERE u.id != :currentUserId 
    AND u.active = true 
    AND (:preferredGender = 'EVERYONE' OR CAST(u.gender AS string) = :preferredGender)
    AND (u.id NOT IN (SELECT s.swiped.id FROM Swipe s WHERE s.swiper.id = :currentUserId))
    AND (u.birthDate IS NOT NULL AND 
        function('date_part', 'year', function('age', u.birthDate)) BETWEEN :minAge AND :maxAge)
    """)
    Page<User> findDiscoveryUsersJPQL(
            @Param("currentUserId") Long currentUserId,
            @Param("preferredGender") String preferredGender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable);
}
