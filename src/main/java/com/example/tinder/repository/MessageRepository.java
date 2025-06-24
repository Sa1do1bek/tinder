package com.example.tinder.repository;

import com.example.tinder.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId")
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);


    // Yangi metod - conversationId va foydalanuvchi ID si bo'yicha xabarlar
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.sender.id = :senderId")
    List<Message> findByConversationIdAndSenderId(
            @Param("conversationId") Long conversationId,
            @Param("senderId") Long senderId
    );

    // Yangi metod - oxirgi xabarlarni olish uchun
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findLatestMessages(@Param("conversationId") Long conversationId);
}