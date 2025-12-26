package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.ConversationDTO;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.ConversationMapper;
import com.example.tinder.repository.ConversationRepository;
import com.example.tinder.repository.MatchRepository;
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


    // Done
    @Override
    public ApiResponse<?> createConversation(Long user1Id, Long user2Id) {
        System.out.println("❗ getOrCreateConversation called with: " + user1Id + " and " + user2Id);

        Optional<Conversation> optional = findConversationBetween(user1Id, user2Id);

        if (optional.isPresent()) {
            System.out.println("✅ Conversation already exists");

            return ApiResponse.success("Conversation already exists Between User1Id = " + user1Id + " and " + user2Id);
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
                .messages(new ArrayList<>())
                .build();

        Conversation saved = conversationRepository.save(conversation);
        return ApiResponse.success("Conversation created successfully with id: " + saved.getId());
    }


    //DONE
    @Override
    public ApiResponse<?> findConversationBetweenUsers(Long currentUserId, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(currentUserId, user2Id);

        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);

        Conversation conversation = optional.get();
        if (currentUserId.equals(conversation.getUser1().getId())) {
            return ApiResponse.success(conversationMapper.toDto(conversation, conversation.getUser2(), conversation.getLastMessageForUser1()), "SUCCESS");
        }

        return ApiResponse.success(conversationMapper.toDto(conversation, conversation.getUser1(), conversation.getLastMessageForUser2()), "SUCCESS");
    }

    @Override
    public ApiResponse<?> deleteConversationForMe(Long currentUserId, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(currentUserId, user2Id);
        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);
        Conversation conversation = optional.get();

        if (conversation.getUser1().getId().equals(currentUserId)) {
            conversation.setDeletedForUser1(true);
        } else {
            conversation.setDeletedForUser1(true);
        }
        conversationRepository.save(conversation);

        return ApiResponse.success("Successfully deleted conversation FOR YOU");
    }


    @Override
    public ApiResponse<?> deleteConversation(Long currentUserId, Long user2Id) {

        Optional<Conversation> optional = findConversationBetween(currentUserId, user2Id);

        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND);
        Conversation conversation = optional.get();

        conversation.setDeletedForUser1(true);
        conversation.setDeletedForUser1(true);
        conversation.setVisibility(false);
        conversationRepository.save(conversation);

        return ApiResponse.success("Successfully deleted conversation FOR ALL");
    }

    @Override
    public ApiResponse<?> getUserAllUnreadCounts(Long currentUserId) {

        Optional<User> user = userRepository.findById(currentUserId);
        if (user.isEmpty()) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);

        ApiResponse<?> userConversations = getUserConversations(currentUserId);
        if (userConversations.getData() == null) return ApiResponse.success(0, "No conversations found");
        if (((List<?>) userConversations.getData()).isEmpty()) return ApiResponse.success(0, "No conversations found");

        List<ConversationDTO> dtos = (List<ConversationDTO>) userConversations.getData();
        Integer result = 0;
        for (ConversationDTO dto : dtos) {
            result += dto.getUnreadCount();
        }

        return ApiResponse.success(result, "Successfully conversations found");
    }


    //DONE
    @Override
    public ApiResponse<?> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        List<ConversationDTO> dtos = new ArrayList<>();
        for (Conversation conversation : conversations) {
            if (conversation.getUser1().getId().equals(userId)) {
                dtos.add(conversationMapper.toDto(conversation, conversation.getUser2(), conversation.getLastMessageForUser1()));
            } else {
                dtos.add(conversationMapper.toDto(conversation, conversation.getUser1(), conversation.getLastMessageForUser2()));
            }
        }
        return ApiResponse.success(dtos, "Conversations retrieved successfully");
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

}
