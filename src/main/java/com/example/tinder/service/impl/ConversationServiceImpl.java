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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ConversationMapper conversationMapper;


    @Override
    public ApiResponse<?> getOrCreateConversation(Long user1Id, Long user2Id) {
        System.out.println("❗ getOrCreateConversation called with: " + user1Id + " and " + user2Id);

        Optional<Conversation> optional = findConversationBetween(user1Id, user2Id);
        if (optional.isPresent()) {
            System.out.println("✅ Conversation already exists");

            return ApiResponse.success(toDto(optional.get()), "Conversation already exists");
        }

        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new IllegalArgumentException("User1 not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new IllegalArgumentException("User2 not found"));

        Conversation conversation = Conversation.builder()
                .user1(user1)
                .user2(user2)
                .isActive(true)
                .build();

        Conversation saved = conversationRepository.save(conversation);

        System.out.println("✅ Conversation created with id: " + saved.getId());

        return ApiResponse.success(toDto(saved), "Conversation created successfully");
    }


    @Override
    public ApiResponse<?> findConversationBetweenUsers(Long user1Id, Long user2Id) {
        Optional<Conversation> optional = findConversationBetween(user1Id, user2Id);

        return optional.map(conversation ->
                ApiResponse.success(toDto(conversation) , "Conversation found")
        ).orElseThrow( () -> RestException.restThrow(ErrorType.CONVERSATION_NOT_FOUND));
//
    }


    @Override
    public ApiResponse<?> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        List<ConversationDTO> dtoList = conversations.stream()
                .map(conversationMapper::toDto)
                .toList();

        return ApiResponse.success(dtoList, "Conversations retrieved successfully");
    }


    @Override
    public Optional<Conversation> getOrCreateConversationEntity(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new IllegalArgumentException("User1 not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new IllegalArgumentException("User2 not found"));


        if (!user1.isActive() || !user2.isActive()) return Optional.empty();


        return findConversationBetween(user1Id, user2Id).or(() -> {

            if (user1 == null || user2 == null) return Optional.empty();

            Conversation conversation = Conversation.builder()
                    .user1(user1)
                    .user2(user2)
                    .isActive(true)
                    .build();

            return Optional.of(conversationRepository.save(conversation));
        });
    }


    @Override
    public boolean isMatchExists(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElse(null);
        User user2 = userRepository.findById(user2Id).orElse(null);

        return user1 != null && user2 != null && matchRepository.existsByUsers(user1, user2);
    }


    private Optional<Conversation> findConversationBetween(Long user1Id, Long user2Id) {
        return conversationRepository.findBetweenUsers(user1Id, user2Id);
    }


    private ConversationDTO toDto(Conversation conversation) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .user1Id(conversation.getUser1().getId())
                .user1Name(conversation.getUser1().getFullname())
                .user2Id(conversation.getUser2().getId())
                .user2Name(conversation.getUser2().getFullname())
                .build();
    }

    private ApiResponse<?> isOnlineChatPartner(Long conversationId) {
        return null;
    }
}
