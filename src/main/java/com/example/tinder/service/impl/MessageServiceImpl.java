package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.ChatMessageDto;
import com.example.tinder.dto.MessageDto;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.Message;
import com.example.tinder.entity.User;
import com.example.tinder.repository.ConversationRepository;
import com.example.tinder.repository.MessageRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ConversationService;
import com.example.tinder.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ApiResponse sendMessage(ChatMessageDto chatMessageDto) {
        try {
            User sender = getUserOrThrow(chatMessageDto.getSenderId());
            User receiver = getUserOrThrow(chatMessageDto.getReceiverId());
            if (!sender.isActive()) {
                return ApiResponse.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .message("Sender account is inactive")
                        .build();
            }

            if (!receiver.isActive()) {
                return ApiResponse.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .message("Receiver account is inactive")
                        .build();
            }
            Conversation conversation = conversationService
                    .getOrCreateConversationEntity(sender.getId(), receiver.getId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found or could not be created"));

            Message message = Message.builder()
                    .sender(sender)
                    .recipient(receiver)
                    .message(chatMessageDto.getMessage())
                    .conversation(conversation)
                    .isRead(false)
                    .build();

            Message saved = messageRepository.save(message);

            Map<String, Object> messageData = buildMessageData(saved, sender, conversation);
            sendToWebSocket(conversation.getId(), sender.getId(), receiver.getId(), messageData);

            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Message sent successfully")
                    .data(toDto(saved))
                    .build();

        } catch (Exception e) {
            return ApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Failed to send message: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public List<MessageDto> getMessageByConversationId(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found"));

        return messageRepository.findByConversationId(conversationId).stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public Optional<Conversation> findConversationEntity(Long user1Id, Long user2Id) {
        return conversationRepository
                .findByUsers(user1Id, user2Id);
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    private MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullname())
                .message(message.getMessage())
                .timestamp(message.getCreatedAt())
                .isRead(message.getIsRead())
                .build();
    }

    private Map<String, Object> buildMessageData(Message message, User sender, Conversation conversation) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("senderId", sender.getId());
        data.put("senderName", sender.getFullname());
        data.put("senderAvatar", sender.getProfilePic());
        data.put("message", message.getMessage());
        data.put("conversationId", conversation.getId());
        data.put("timestamp", message.getCreatedAt());
        data.put("isRead", message.getIsRead());
        return data;
    }

    private void sendToWebSocket(Long conversationId, Long senderId, Long receiverId, Map<String, Object> messageData) {
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, messageData);
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", messageData);
        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", messageData);
    }
}
