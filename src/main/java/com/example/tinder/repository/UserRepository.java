package com.example.tinder.repository;

import com.example.tinder.entity.User;
import com.example.tinder.enums.Gender;
import jakarta.transaction.Transactional;
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
public interface UserRepository extends JpaRepository<User,Long> {

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



}
