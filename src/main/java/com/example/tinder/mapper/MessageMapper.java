package com.example.tinder.mapper;

import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.Message;
import com.example.tinder.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageMapper {

    public MessageDto toDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setMessage(message.getMessage());
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getRecipient().getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setEdited(message.getEdited());
        dto.setIsLiked(message.getIsLiked());
        dto.setDeletedForSender(message.getDeletedForSender());
        dto.setDeletedForRecipient(message.getDeletedForRecipient());
        dto.setIsRead(message.getIsRead());
        dto.setSenderName(message.getSender().getFullname());
        return dto;
    }

    public Message fromCreateDto(MessageDto.MessageCreateDto dto, User sender, User recipient, Conversation conversation) {
        Message message = new Message();
        message.setMessage(dto.getMessage());
        message.setSender(sender);
        message.setRecipient(recipient);

        message.setIsLiked(false);
        message.setEdited(false);
        message.setIsRead(false);
        message.setConversation(conversation);
        message.setDeletedForSender(false);
        message.setDeletedForRecipient(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        message.setVisibility(true);

        return message;
    }
}
