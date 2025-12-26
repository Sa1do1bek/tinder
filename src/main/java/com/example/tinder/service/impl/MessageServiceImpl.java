package com.example.tinder.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

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

        boolean matchExists = conversationService.isMatchExists(sender.getId(), receiver.getId());
        if (!matchExists) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);

        Conversation c = conversationRepository.findByUsers(sender.getId(), receiver.getId()).orElseThrow(
                () -> RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND));

        Conversation conversation = conversationRepository.save(c);

        Message message = messageRepository.save(messageMapper.fromCreateDto(req, sender, receiver, conversation));



        //LastMessage ni o'rnatish
        if (message.getSender().getId().equals(sender.getId())) {
            message.setDeletedForSender(true);
        }else {
            message.setDeletedForRecipient(true);
        }

        Message save = messageRepository.save(message);

        Optional<Message> senderLastMessage = messageRepository.findLastMessageByConversationIdForSender(message.getConversation().getId(), sender.getId());
        Optional<Message> recipientLastMessage = messageRepository.findLastMessageByConversationIdForSender(message.getConversation().getId(), sender.getId());

        if (senderLastMessage.isPresent() && recipientLastMessage.isPresent()) {
            if (conversation.getUser1().getId().equals(sender.getId())){
                conversation.setLastMessageForUser1(senderLastMessage.get().getMessage());
            }else {
                conversation.setLastMessageForUser2(recipientLastMessage.get().getMessage());
            }

            if (conversation.getUser2().getId().equals(sender.getId())){
                conversation.setLastMessageForUser2(senderLastMessage.get().getMessage());
            }else {
                conversation.setLastMessageForUser1(recipientLastMessage.get().getMessage());
            }
        }
        conversationRepository.save(conversation);



        // Sending Websocket
        sendToWebSocket(conversation.getId(), sender.getId(), receiver.getId(), messageMapper.toDto(save));

        return ApiResponse.success(messageMapper.toDto(save), "MESSAGE_SENT_SUCCESSFULLY");
    }


    @Override
    public Page<MessageDto> getMessagesByConversationId(Long conversationId, User currentUser, Pageable pageable) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND));

        messageRepository.markAsRead(conversationId, currentUser.getId());

        Page<Message> page = messageRepository.findByConversationId(conversationId, pageable);

        return page.map(messageMapper::toDto);
    }


    @Override
    public ApiResponse<?> deleteMessageForMe(Long messageId, User currentUser) {

        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) {
            throw RestException.restThrow(ErrorType.MESSAGE_NOT_FOUND);
        }

        Optional<User> optionalUser = userRepository.findById(currentUser.getId());
        if (optionalUser.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }


        User user = optionalUser.get();
        Message message = optionalMessage.get();

        Optional<Conversation> betweenUsers = conversationRepository.findBetweenUsers(message.getRecipient().getId(), message.getSender().getId());
        if (betweenUsers.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);

        Conversation conversation = betweenUsers.get();

        //lastMessage ni o'rnatish
        if (message.getSender().getId().equals(user.getId())) {
           message.setDeletedForSender(true);
        }else {
            message.setDeletedForRecipient(true);
        }

        Message save = messageRepository.save(message);

        Optional<Message> lastMessage = messageRepository.findLastMessageByConversationIdForSender(message.getConversation().getId(), user.getId());
        if (lastMessage.isPresent()) {
            if (conversation.getUser1().getId().equals(user.getId())){
                conversation.setLastMessageForUser1(lastMessage.get().getMessage());
            }else {
                conversation.setLastMessageForUser2(lastMessage.get().getMessage());
            }
        }
        conversationRepository.save(conversation);

        return ApiResponse.success(messageMapper.toDto(save), "MESSAGE_DELETED_SUCCESSFULLY");
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdCustom(userId).orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
    }



    private void sendToWebSocket(Long conversationId, Long senderId, Long receiverId, MessageDto messageData) {
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, messageData);
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", messageData);
        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", messageData);
    }
}
