package com.example.tinder.repository;

import com.example.tinder.entity.Match;
import com.example.tinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Match m " +
            "WHERE (m.user1 = :user1 AND m.user2 = :user2) OR (m.user1 = :user2 AND m.user2 = :user1) AND m.visibility = true")
    boolean existsByUsers(@Param("user1") User user1, @Param("user2") User user2);



    @Transactional
    @Modifying
    @Query("""
    UPDATE Match m
    SET m.visibility = false
    WHERE
      (m.user1.id = :u1 AND m.user2.id = :u2)
      OR
      (m.user1.id = :u2 AND m.user2.id = :u1)
""")
    void deactivateMatch(Long u1, Long u2);
}