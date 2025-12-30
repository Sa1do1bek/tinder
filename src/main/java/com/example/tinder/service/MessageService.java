package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface MessageService {

    ApiResponse<?> sendMessage(MessageDto.MessageCreateDto chatMessageDto);

    ApiResponse<?>  getMessagesByConversationId(Long conversationId, User currentUser, Pageable pageable);

    ApiResponse<?> deleteMessageForAll(Long messageId, User currentUser);

    ApiResponse<?> deleteMessageForMe(Long messageId, User currentUser);

    ApiResponse<?> updateMessage(MessageDto.MessageUpdateDto chatMessageDto, User currentUser);

    ApiResponse<?> likeMessage(Long messageId, Boolean isLike, User currentUser);

    ApiResponse<?> markMessageAsRead(Long conversationId, User currentUser);

    ApiResponse<?> uploadMediaMessage(User user, List<MultipartFile> files);
}
