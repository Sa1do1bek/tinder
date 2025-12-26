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
                ORDER BY m.createdAt DESC
            """)
    Page<Message> findByConversationId(
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


    @Query(value = """
                SELECT *
                FROM message
                WHERE conversation_id = :conversationId
                  AND recipient_id = :recipientId
                  AND deleted_for_recipient = false
                ORDER BY created_at DESC
                LIMIT 1
            """, nativeQuery = true)
    Optional<Message> findLastMessageByConversationIdForRecipient(
            @Param("conversationId") Long conversationId,
            @Param("recipientId") Long recipientId
    );


    @Query(value = """
                SELECT * FROM message
                WHERE conversation_id = :conversationId
                  AND sender_id = :senderId
                  AND deleted_for_recipient = false
                ORDER BY created_at DESC
                LIMIT 1
            """, nativeQuery = true)
    Optional<Message> findLastMessageByConversationIdForSender(@Param("conversationId") Long conversationId,
                                                               @Param("senderId") Long senderId);
}