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










    @Query(value = """
    SELECT u.*, 
    (SELECT COUNT(*) FROM users_user_interests uui
     WHERE uui.user_id = u.id AND uui.user_interests_id IN 
     (SELECT user_interests_id FROM users_user_interests WHERE user_id = :currentUserId)
    ) as match_score,
    (6371 * acos(
        least(1.0, greatest(-1.0, 
            cos(radians(:lat)) * cos(radians(CAST(ul.latitude AS double precision))) *
            cos(radians(CAST(ul.longitude AS double precision)) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(CAST(ul.latitude AS double precision)))
        ))
    )) as distance_val
    FROM users u
    JOIN users_location ul ON u.id = ul.user_id
    WHERE u.id != :currentUserId
    AND u.active = true
    AND (:preferredGender = 'EVERYONE' OR u.gender = :preferredGender)
    AND (EXTRACT(YEAR FROM AGE(u.birth_date)) BETWEEN :minAge AND :maxAge)
    AND u.id NOT IN (SELECT swiped_id FROM swipe WHERE swiper_id = :currentUserId)
    AND (6371 * acos(
        least(1.0, greatest(-1.0, 
            cos(radians(:lat)) * cos(radians(CAST(ul.latitude AS double precision))) *
            cos(radians(CAST(ul.longitude AS double precision)) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(CAST(ul.latitude AS double precision)))
        ))
    )) <= :maxDistance
    ORDER BY match_score DESC, distance_val ASC
    """,
            countQuery = "SELECT count(*) FROM users u JOIN users_location ul ON u.id = ul.user_id WHERE u.id != :currentUserId AND u.active = true",
            nativeQuery = true)
    Page<User> findDiscoveryUsersWithSmartMatching(
            @Param("currentUserId") Long currentUserId,
            @Param("preferredGender") String preferredGender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("maxDistance") Double maxDistance,
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            Pageable pageable);




    @Query(value = """
    SELECT u.*, 
    (SELECT COUNT(*) FROM users_user_interests uui 
     WHERE uui.user_id = u.id AND uui.user_interests_id IN 
     (SELECT user_interests_id FROM users_user_interests WHERE user_id = :currentUserId)
    ) as match_score
    FROM users u
    WHERE u.id != :currentUserId 
    AND u.active = true 
    AND (:preferredGender = 'EVERYONE' OR u.gender = :preferredGender)
    AND (EXTRACT(YEAR FROM AGE(u.birth_date)) BETWEEN :minAge AND :maxAge)
    AND u.id NOT IN (SELECT swiped_id FROM swipe WHERE swiper_id = :currentUserId)
    ORDER BY match_score DESC, u.fullname ASC
    """,
            countQuery = """
    SELECT count(*) FROM users u 
    WHERE u.id != :currentUserId AND u.active = true
    AND (:preferredGender = 'EVERYONE' OR u.gender = :preferredGender)
    AND (EXTRACT(YEAR FROM AGE(u.birth_date)) BETWEEN :minAge AND :maxAge)
    AND u.id NOT IN (SELECT swiped_id FROM swipe WHERE swiper_id = :currentUserId)
    """,
            nativeQuery = true)
    Page<User> findDiscoveryUsersWithoutLocation(
            @Param("currentUserId") Long currentUserId,
            @Param("preferredGender") String preferredGender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable);







    @Query(value = """
        SELECT DISTINCT u.* FROM users u
        JOIN users_location ul ON u.id = ul.user_id
        JOIN users_user_interests uui ON u.id = uui.user_id
        WHERE u.id != :currentUserId
        AND u.active = true
        AND uui.user_interests_id = :interestId
        AND (:preferredGender = 'EVERYONE' OR u.gender = :preferredGender)
        AND (u.birth_date IS NOT NULL AND EXTRACT(YEAR FROM AGE(u.birth_date)) BETWEEN :minAge AND :maxAge)
        AND u.id NOT IN (SELECT swiped_id FROM swipe WHERE swiper_id = :currentUserId)
        AND (6371 * acos(
            least(1.0, greatest(-1.0, 
                cos(radians(:lat)) * cos(radians(CAST(ul.latitude AS double precision))) *
                cos(radians(CAST(ul.longitude AS double precision)) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(CAST(ul.latitude AS double precision)))
            ))
        )) <= :maxDistance
        """,
            countQuery = """
            SELECT count(DISTINCT u.id) FROM users u 
            JOIN users_user_interests uui ON u.id = uui.user_id 
            WHERE u.id != :currentUserId AND uui.user_interests_id = :interestId
        """,
            nativeQuery = true)
    Page<User> findUsersBySpecificInterest(
            @Param("currentUserId") Long currentUserId,
            @Param("interestId") Long interestId,
            @Param("preferredGender") String preferredGender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("maxDistance") Double maxDistance,
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            Pageable pageable);


    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM users_user_interests
            WHERE user_id = :userId
            """, nativeQuery = true)
    void deleteUserInterests(@Param("userId") Long userId);
}
