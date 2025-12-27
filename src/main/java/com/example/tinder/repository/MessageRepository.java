package com.example.tinder.repository;

import com.example.tinder.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {


    @Query("""
                SELECT m FROM Message m
                WHERE m.id = :id
                AND m.visibility = true
            """)
    @NotNull Optional<Message> findById(@Param("id") @NotNull Long id);


    @Query("""
                  SELECT m FROM Message m
                   WHERE m.conversation.id = :conversationId
                     AND m.visibility = true
                     AND (
                           (m.sender.id = :currentUserId AND m.deletedForSender = false)
                        OR (m.recipient.id = :currentUserId AND m.deletedForRecipient = false)
                     )
                   ORDER BY m.createdAt DESC
            """)
    Page<Message> findMessagesForUserInConversation(
            @Param("currentUserId") Long currentUserId,
            @Param("conversationId") Long conversationId,
            Pageable pageable
    );


    @Transactional
    @Modifying
    @Query("""
                UPDATE Message m
                SET m.isRead = true
                WHERE m.conversation.id = :conversationId
                  AND m.recipient.id = :userId
                  AND m.isRead = false
            """)
    void markAsRead(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );


    @Transactional
    @Modifying
    @Query("""
    UPDATE Message m
    SET m.deletedForSender = true,
        m.deletedForRecipient = true
    WHERE m.id = :messageId
      AND m.sender.id = :senderId
""")
    int deleteForAll(
            @Param("messageId") Long messageId,
            @Param("senderId") Long senderId
    );




    @Query(value = """
                SELECT * FROM message
                WHERE conversation_id = :conversationId
                  AND visibility = true            
                  AND  (
                           (sender_id = :userId AND deleted_for_sender = false)
                        OR (recipient_id = :userId AND deleted_for_recipient = false)
                       )
                ORDER BY created_at DESC
                LIMIT 1
            """, nativeQuery = true)
    Optional<Message> findLastVisibleMessageForUser(@Param("conversationId") Long conversationId,
                                                               @Param("userId") Long userId);
}