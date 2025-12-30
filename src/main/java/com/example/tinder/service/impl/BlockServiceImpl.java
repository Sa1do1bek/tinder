package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Block;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.BlockMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final BlockMapper blockMapper;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public ApiResponse<?> blockUser(Long blockerId, Long blockedId) {

        if (blockerId.equals(blockedId)) {
            throw RestException.restThrow(ErrorType.INVALID_OPERATION);
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())) {
            throw RestException.restThrow(ErrorType.ALREADY_BLOCKED);
        }

        blockRepository.save(new Block(blocker, blocked));

        swipeRepository.hideSwipes(blocker.getId(), blocked.getId());

        matchRepository.deactivateMatch(blocker.getId(), blocked.getId());

        conversationRepository.findBetweenUsers(blocker.getId(), blocked.getId())
                .ifPresent(conversation -> {
                    messageRepository.hideAllMessages(conversation.getId());
                    conversation.setVisibility(false);
                    conversation.setUnreadForUser1(0);
                    conversation.setUnreadForUser2(0);
                    conversationRepository.save(conversation);
                });


        return ApiResponse.success("USER BLOCKED SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> unblockUser(Long blockerId, Long blockedId) {

        if (blockerId.equals(blockedId)) {
            throw RestException.restThrow(ErrorType.INVALID_OPERATION);
        }

        Block block = blockRepository
                .findByBlockerIdAndBlockedId(blockerId, blockedId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.BLOCK_NOT_FOUND));

        blockRepository.delete(block);
        return ApiResponse.success("USER UNBLOCKED SUCCESSFULLY");
    }

    /**
     * user1 user2 ni block qilganmi
     * yoki user2 user1 ni block qilganmi
     */
    @Override
    public boolean isBlocked(Long userId1, Long userId2) {
        return blockRepository.existsByBlockerIdAndBlockedId(userId1, userId2)
            || blockRepository.existsByBlockerIdAndBlockedId(userId2, userId1);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getUserBlocks(Long userId) {
        return ApiResponse.success(blockRepository.findAllUserBlocks(userId)
                .stream()
                .map(blockMapper::toDto)
                .toList(), "SUCCESS");
    }
}