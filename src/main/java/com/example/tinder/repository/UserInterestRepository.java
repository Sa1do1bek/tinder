package com.example.tinder.repository;

import com.example.tinder.entity.UserInterest;
import jakarta.transaction.Transactional;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    @Modifying
    @Transactional
    @Query("delete from UserInterest ui where ui.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query(value = """
    SELECT i FROM UserInterest i
    WHERE i.code = :code
""")
    Optional<UserInterest> findByCode(@Param("code") String code);
}