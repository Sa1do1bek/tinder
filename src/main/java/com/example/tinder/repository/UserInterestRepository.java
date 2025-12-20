package com.example.tinder.repository;

import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.CategoryType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

//    @Modifying
//    @Transactional
//    @Query("delete from UserInterest ui where ui.userId = :userId")
//    void deleteAllByUserId(@Param("userId") Long userId);


    @Query(value = """
                SELECT i FROM UserInterest i
                WHERE i.code = :code
                AND i.visibility = true
            """)
    Optional<UserInterest> findByCode(@Param("code") String code);


    @Query(value = """
                SELECT i FROM UserInterest i
                WHERE i.categoryType = :type
                AND i.visibility = true
            """)
    List<UserInterest> findByCategoryType(@Param("type") CategoryType type);


    @Query("""
        SELECT ui
        FROM User u JOIN u.userInterests ui
        WHERE u.id = :userId
    """)
    List<UserInterest> findByUserId(Long userId);


    @Query(value = """
                SELECT ui FROM User u 
                JOIN u.userInterests ui
                WHERE u.id = :userId
                AND ui.categoryType = :type
                AND ui.visibility = true
            """)
    List<UserInterest> findByUserIdAndType(Long userId, CategoryType type);
}












