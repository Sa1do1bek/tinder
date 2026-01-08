package com.example.tinder.repository;

import com.example.tinder.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("""
        SELECT c FROM Card c
                WHERE c.user.id = :userId
        """)
    Optional<Card> findByUserId(@Param("userId") Long userId);

    Optional<Card> findByNumber(String number);


}