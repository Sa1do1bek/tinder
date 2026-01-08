package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.ConversationDTO;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.ConversationMapper;
import com.example.tinder.repository.ConversationRepository;
import com.example.tinder.repository.MatchRepository;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ConversationMapper conversationMapper;
    private final SwipeRepository swipeRepository;


    @Override
    public ApiResponse<?> createConversation(Long user1Id, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(user1Id, user2Id);
        if (optional.isPresent() && optional.get().getVisibility()){
            optional.get().setIsMatchedForUser1(true);
            optional.get().setIsMatchedForUser2(true);
            conversationRepository.save(optional.get());
            throw RestException.restThrow(ErrorType.CONVERSATION_ALREADY_EXISTS);
        }

        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Conversation conversation = Conversation.builder()
                .user1(user1)
                .user2(user2)
                .deletedForUser1(false)
                .deletedForUser2(false)
                .isMatchedForUser1(true)
                .isMatchedForUser2(true)
                .messages(new ArrayList<>())
                .unreadForUser1(0)
                .unreadForUser2(0)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        return ApiResponse.success(conversationMapper.toDto(saved), SuccessType.CONVERSATION_CREATED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> createConversationDirectly(Long user1Id, Long user2Id) {

        Optional<Conversation> optional = findBetweenUsersForDirectly(user1Id, user2Id);

        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (optional.isPresent()) {
            Conversation c = optional.get();

            if (c.getUser1().equals(user1)) {
                c.setDeletedForUser1(false);
                c.setIsMatchedForUser1(true);
                Conversation save = conversationRepository.save(c);
                return ApiResponse.success(conversationMapper.toDto(save,
                        user1,
                        save.getLastMessageForUser1(),
                        save.getUnreadForUser1(),
                        save.getIsMatchedForUser1()),
                        SuccessType.CONVERSATION_ALREADY_EXISTS);
            } else if (c.getUser2().equals(user1)) {
                c.setDeletedForUser2(false);
                c.setIsMatchedForUser2(true);
                Conversation save = conversationRepository.save(c);
                return ApiResponse.success(conversationMapper.toDto(save,
                                user2,
                                save.getLastMessageForUser2(),
                                save.getUnreadForUser2(),
                                save.getIsMatchedForUser2()),
                        SuccessType.CONVERSATION_ALREADY_EXISTS);

            }
        }

        Conversation conversation = Conversation.builder()
                .user1(user1)
                .user2(user2)
                .deletedForUser1(false)
                .deletedForUser2(false)
                .isMatchedForUser1(true)
                .isMatchedForUser2(false)
                .messages(new ArrayList<>())
                .unreadForUser1(0)
                .unreadForUser2(0)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        return ApiResponse.success(conversationMapper.toDto(saved),SuccessType.CONVERSATION_CREATED_SUCCESSFULLY);
    }


    @Override
    public ApiResponse<?> markAsMatchConversation(Long conversationId) {

        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);

        Conversation conversation = optional.get();
        conversation.setIsMatchedForUser1(true);
        conversation.setIsMatchedForUser2(true);
        Conversation save = conversationRepository.save(conversation);

        return ApiResponse.success(conversationMapper.toDto(save), SuccessType.CONVERSATION_MARKED_AS_MATCH_SUCCESSFULLY);
    }


    //DONE
    @Override
    public ApiResponse<?> findConversationBetweenUsers(Long currentUserId, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(currentUserId, user2Id);

        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);

        Conversation conversation = optional.get();
        if (currentUserId.equals(conversation.getUser1().getId())) {
            return ApiResponse.success(conversationMapper.toDto(conversation,
                    conversation.getUser2(),
                    conversation.getLastMessageForUser1(),
                    conversation.getUnreadForUser1(),
                    conversation.getIsMatchedForUser1()),
                    SuccessType.CONVERSATION_FOUND_SUCCESSFULLY);
        }

        return ApiResponse.success(conversationMapper.toDto(conversation, conversation.getUser1(),
                conversation.getLastMessageForUser2(),
                conversation.getUnreadForUser2(),
                conversation.getIsMatchedForUser2()),
                SuccessType.CONVERSATION_FOUND_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> deleteConversationForMe(Long currentUserId, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(currentUserId, user2Id);
        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);
        Conversation conversation = optional.get();

        if (conversation.getUser1().getId().equals(currentUserId)) {
            conversation.setDeletedForUser1(true);
        } else {
            conversation.setDeletedForUser2(true);
        }
        conversationRepository.save(conversation);

        return ApiResponse.success(SuccessType.CONVERSATION_DELETED_SUCCESSFULLY_FOR_YOU);
    }


    @Override
    public ApiResponse<?> deleteConversation(Long currentUserId, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(currentUserId, user2Id);

        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);
        Conversation conversation = optional.get();

        conversation.setDeletedForUser1(true);
        conversation.setDeletedForUser2(true);
        conversation.setIsMatchedForUser1(false);
        conversation.setIsMatchedForUser2(false);
        conversation.setVisibility(false);
        conversationRepository.save(conversation);

        swipeRepository.hideSwipes(conversation.getUser1().getId(), conversation.getUser2().getId());



        return ApiResponse.success(SuccessType.CONVERSATION_DELETED_SUCCESSFULLY_FOR_ALL);
    }

    @Override
    public ApiResponse<?> getUserAllUnreadCounts(Long currentUserId) {

        Optional<User> user = userRepository.findById(currentUserId);
        if (user.isEmpty()) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);

        ApiResponse<?> userConversations = getUserConversations(currentUserId);
        if (userConversations.getData() == null) return ApiResponse.success(0, SuccessType.NO_CONVERSATIONS_FOUND);
        if (((List<?>) userConversations.getData()).isEmpty()) return ApiResponse.success(0, SuccessType.NO_CONVERSATIONS_FOUND);

        List<ConversationDTO> dtos = (List<ConversationDTO>) userConversations.getData();
        Integer result = 0;
        for (ConversationDTO dto : dtos) {
            result += dto.getUnreadCount();
        }

        return ApiResponse.success(result, SuccessType.USER_ALL_UNREAD_COUNTS_FOUND_SUCCESSFULLY);
    }


    @Override
    public ApiResponse<?> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        List<ConversationDTO> dtos = new ArrayList<>();
        for (Conversation conversation : conversations) {
            if (conversation.getUser1().getId().equals(userId)) {
                dtos.add(conversationMapper.toDto(
                        conversation,
                        conversation.getUser2(),
                        conversation.getLastMessageForUser1(),
                        conversation.getUnreadForUser1(),
                        conversation.getIsMatchedForUser1()));
            } else {
                dtos.add(conversationMapper.toDto(
                        conversation,
                        conversation.getUser1(),
                        conversation.getLastMessageForUser2(),
                        conversation.getUnreadForUser2(),
                        conversation.getIsMatchedForUser2()));
            }
        }
        return ApiResponse.success(dtos, SuccessType.USER_CONVERSATIONS_FETCHED_SUCCESSFULLY);
    }


    //DONE
    @Override
    public boolean isMatchExists(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElse(null);
        User user2 = userRepository.findById(user2Id).orElse(null);

        return user1 != null && user2 != null && matchRepository.existsByUsers(user1, user2);
    }


    private Optional<Conversation> findConversationBetween(Long user1Id, Long user2Id) {
        return conversationRepository.findBetweenUsers(user1Id, user2Id);
    }

    private Optional<Conversation> findBetweenUsersForDirectly(Long user1Id, Long user2Id) {
        return conversationRepository.findBetweenUsersForDirectly(user1Id, user2Id);
    }
}
