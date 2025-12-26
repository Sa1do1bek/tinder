package com.example.tinder.repository;

import com.example.tinder.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {


    @Query("""
                SELECT c FROM Conversation c 
                WHERE (c.user1.id = :user1Id AND c.user2.id = :user2Id AND c.deletedForUser1 = false)  
                   OR (c.user1.id = :user2Id AND c.user2.id = :user1Id AND c.deletedForUser2 = false)
                AND c.visibility = true
            """)
    Optional<Conversation> findBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );


    // Foydalanuvchining barcha conversationlari
    @Query("""
                SELECT c FROM Conversation c
                WHERE (c.user1.id = :userId AND c.deletedForUser1 = false)
                OR (c.user2.id = :userId AND c.deletedForUser2 = false)
                AND c.visibility = true
            """)
    List<Conversation> findAllByUserId(@Param("userId") Long userId);




    @Query("""
            SELECT c FROM Conversation c
            WHERE (c.user1.id = :userId AND c.deletedForUser1 = false)
            OR (c.user2.id = :userId AND c.deletedForUser2 = false)
            AND c.visibility = true
            """)
    List<Conversation> findByUserId(@Param("userId") Long userId);




    @Query("""
            SELECT c FROM Conversation c
            WHERE ((c.user1.id = :user1Id AND c.deletedForUser1 = false)
                  AND c.user2.id = :user2Id AND c.deletedForUser2 = false)
            OR
                  ((c.user1.id = :user2Id AND c.deletedForUser2 = false) 
                  AND (c.user2.id = :user1Id AND c.deletedForUser1 = false))
            AND c.visibility = true
            """)
    Optional<Conversation> findByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

}
