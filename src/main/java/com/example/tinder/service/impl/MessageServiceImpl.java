package com.example.tinder.service.impl;

import com.example.tinder.config.WebSocketEventListener;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.Message;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.MessageMapper;
import com.example.tinder.repository.ConversationRepository;
import com.example.tinder.repository.MessageRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ConversationService;
import com.example.tinder.service.MessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketEventListener eventListener;

    @Override
    @Transactional
    public ApiResponse<?> sendMessage(MessageDto.MessageCreateDto req) {


        User sender = getUserOrThrow(req.getSenderId());
        User receiver = getUserOrThrow(req.getReceiverId());

        if (!sender.isActive()) {
            throw RestException.restThrow(ErrorType.SENDER_ACCOUNT_INACTIVE);
        }

        if (!receiver.isActive()) {
            throw RestException.restThrow(ErrorType.RECEIVER_ACCOUNT_INACTIVE);
        }

        Conversation conversation = conversationRepository
                .findByUsers(sender.getId(), receiver.getId())
                .orElseThrow(() ->
                        RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND));

        Message message = messageMapper.fromCreateDto(req, sender, receiver, conversation);


        Message savedMessage = messageRepository.save(message);

        // Update last messages
        updateConversationLastMessage(conversation, sender, receiver, savedMessage);

        // ✅ WebSocket orqali yuborish - faqat conversation topic'ga
        MessageDto messageDto = messageMapper.toDto(savedMessage);
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversation.getId(),
                messageDto
        );


        log.info("📤 Message sent: conversationId={}, from={}, to={}",
                conversation.getId(), sender.getUsername(), receiver.getUsername());

        return ApiResponse.success(messageDto, "MESSAGE_SENT_SUCCESSFULLY");
    }


    @Override
    public ApiResponse<?> getMessagesByConversationId(Long conversationId, User currentUser, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND));

        // optional: user conversationga tegishli ekanini tekshirish
        if (!conversation.getUser1().getId().equals(currentUser.getId())
                && !conversation.getUser2().getId().equals(currentUser.getId())) {
            throw RestException.restThrow(ErrorType.ACCESS_DENIED);
        }

        // o'qilmaganlarni togridan togri socketdan yuborish uchun
        List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(conversationId, currentUser.getId());
        for (Message unreadMessage : unreadMessages) {
            // ✅ WebSocket orqali yuborish - faqat conversation topic'ga
            unreadMessage.setIsRead(true);
            Message save = messageRepository.save(unreadMessage);
            MessageDto messageDto = messageMapper.toDto(save);
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversation.getId(),
                    messageDto
            );
        }


        // faqat recipient bo‘lgan message lar read bo‘ladi
//        messageRepository.markAsRead(conversationId, currentUser.getId());

        Page<Message> page =
                messageRepository.findMessagesForUserInConversation(
                        currentUser.getId(),
                        conversationId,
                        pageable
                );

        Map<String, Object> result = new HashMap<>();
        result.put("messages", page.map(messageMapper::toDto));
        result.put("currentPage", page.getNumber());
        result.put("totalPages", (page.getTotalPages()+1) / 2);

        return ApiResponse.success(result, "MESSAGES_FETCHED_SUCCESSFULLY");

    }


    @Override
    public ApiResponse<?> deleteMessageForMe(Long messageId, User currentUser) {


        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        RestException.restThrow(ErrorType.MESSAGE_NOT_FOUND));

        // Security check
        if (!message.getSender().getId().equals(currentUser.getId())
                && !message.getRecipient().getId().equals(currentUser.getId())) {
            throw RestException.restThrow(ErrorType.ACCESS_DENIED);
        }

        // Delete for me
        if (message.getSender().getId().equals(currentUser.getId())) {
            message.setDeletedForSender(true);
        } else {
            message.setDeletedForRecipient(true);
        }

        messageRepository.save(message);

        Conversation conversation = message.getConversation();

        Optional<Message> lastMessage =
                messageRepository.findLastVisibleMessageForUser(
                        conversation.getId(),
                        currentUser.getId()
                );

        if (conversation.getUser1().getId().equals(currentUser.getId())) {
            conversation.setLastMessageForUser1(
                    lastMessage.map(Message::getMessage).orElse(null)
            );
        } else {
            conversation.setLastMessageForUser2(
                    lastMessage.map(Message::getMessage).orElse(null)
            );
        }

        conversationRepository.save(conversation);

        return ApiResponse.success(
                messageMapper.toDto(message),
                "MESSAGE_DELETED_SUCCESSFULLY"
        );
    }

    @Override
    public ApiResponse<?> updateMessage(MessageDto.MessageUpdateDto dto, User currentUser) {

         Message message = messageRepository.findById(dto.getId())
               .orElseThrow(() -> RestException.restThrow(ErrorType.MESSAGE_NOT_FOUND));

        // Security check
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw RestException.restThrow(ErrorType.ACCESS_DENIED);
        }

        message.setMessage(dto.getMessage());
        message.setEdited(true);
        Message save = messageRepository.save(message);

        return ApiResponse.success(messageMapper.toDto(save), "MESSAGE_UPDATED_SUCCESSFULLY");
    }


    @Transactional
    @Override
    public ApiResponse<?> markMessageAsRead(Long conversationId, User currentUser) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND));

        // 🔐 Security check
        if (!conversation.getUser1().getId().equals(currentUser.getId())
                && !conversation.getUser2().getId().equals(currentUser.getId())) {
            throw RestException.restThrow(ErrorType.ACCESS_DENIED);
        }

        // faqat recipient bo‘lgan message lar read bo‘ladi
        messageRepository.markAsRead(conversationId, currentUser.getId());

        if (conversation.getUser1().getId().equals(currentUser.getId())) {
            conversation.setUnreadForUser1(0);
        }else {
            conversation.setUnreadForUser2(0);
        }
        conversationRepository.save(conversation);
        return ApiResponse.success("MESSAGE_MARKED_AS_READ_SUCCESSFULLY");
    }


    @Transactional
    @Override
    public ApiResponse<?> deleteMessageForAll(Long messageId, User currentUser) {

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        RestException.restThrow(ErrorType.MESSAGE_NOT_FOUND));

        // ❗ Faqat sender delete for all qila oladi
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw RestException.restThrow(ErrorType.ACCESS_DENIED);
        }

        int updated = messageRepository.deleteForAll(
                messageId,
                currentUser.getId()
        );

        if (updated == 0) {
            throw RestException.restThrow(ErrorType.OPERATION_FAILED);
        }

        Conversation conversation = message.getConversation();

        // Sender uchun lastMessage
        messageRepository.findLastVisibleMessageForUser(
                conversation.getId(),
                conversation.getUser1().getId()
        ).ifPresentOrElse(
                m -> conversation.setLastMessageForUser1(m.getMessage()),
                () -> conversation.setLastMessageForUser1(null)
        );

        // Recipient uchun lastMessage
        messageRepository.findLastVisibleMessageForUser(
                conversation.getId(),
                conversation.getUser2().getId()
        ).ifPresentOrElse(
                m -> conversation.setLastMessageForUser2(m.getMessage()),
                () -> conversation.setLastMessageForUser2(null)
        );

        conversationRepository.save(conversation);

        return ApiResponse.success(
                null,
                "MESSAGE_DELETED_FOR_ALL_SUCCESSFULLY"
        );
    }



    private void updateConversationLastMessage(Conversation conv, User sender, User receiver, Message msg) {
        Long senderId = sender.getId();
        Long user1Id = conv.getUser1().getId();

        conv.setLastMessageForUser1(msg.getMessage());
        conv.setLastMessageForUser2(msg.getMessage());

        // last message
        if (senderId.equals(user1Id)) {
            conv.setUnreadForUser2(conv.getUnreadForUser2() + 1);
        } else {
            conv.setUnreadForUser1(conv.getUnreadForUser1() + 1);
        }
        conversationRepository.save(conv);
    }






    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdCustom(userId).orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
    }


    private void sendToWebSocket(Long conversationId, Long senderId, Long receiverId, MessageDto messageData) {
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, messageData);
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", messageData);
        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", messageData);
        System.out.println("Habar Socket orqali yuborildi ");
    }
    //https://api-tinder/ws

    // /topic/chat/" + conversationId
}
