package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.story.StoryResponseDto;
import com.example.tinder.dto.story.StoryViewerDto;
import com.example.tinder.dto.story.UserStoryGroupDto;
import com.example.tinder.entity.Match;
import com.example.tinder.entity.Story;
import com.example.tinder.entity.StoryView;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.StoryMediaType;
import com.example.tinder.enums.StoryVisibility;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.MatchRepository;
import com.example.tinder.repository.StoryRepository;
import com.example.tinder.repository.StoryViewRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.S3Service;
import com.example.tinder.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public ApiResponse<?> createStory(MultipartFile file, StoryMediaType type, StoryVisibility visibility, User user) {
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }

        if (file == null || file.isEmpty()) {
            throw RestException.restThrow(ErrorType.MISSING_PARAMETER);
        }

        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (type == StoryMediaType.IMAGE && fileSizeInMB > 10) {
            throw RestException.restThrow(ErrorType.FILE_SIZE_EXCEEDS_LIMIT);
        } else if (type == StoryMediaType.VIDEO && fileSizeInMB > 50) {
            throw RestException.restThrow(ErrorType.FILE_SIZE_EXCEEDS_LIMIT);
        }

        try {
            String mediaUrl = s3Service.uploadFile(file);
            Story story = Story.builder()
                    .user(user)
                    .mediaUrl(mediaUrl)
                    .mediaType(type)
                    .storyVisibility(visibility)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .viewCount(0)
                    .build();

            storyRepository.save(story);
            return ApiResponse.success(SuccessType.STORY_CREATED_SUCCESSFULLY);
        } catch (Exception e) {
            throw RestException.restThrow(ErrorType.AWS_S3_UNABLE_TO_UPLOAD);
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> viewStory(Long storyId, User viewer) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.STORY_NOT_FOUND));

        if (!story.getVisibility() || story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw RestException.restThrow(ErrorType.STORY_NO_LONGER_AVAILABLE);
        }

        boolean alreadyViewed = storyViewRepository.existsByStoryAndViewer(story, viewer);
        if (!alreadyViewed && !story.getUser().getId().equals(viewer.getId())) {
            StoryView view = StoryView.builder()
                    .story(story)
                    .viewer(viewer)
                    .build();
            storyViewRepository.save(view);

            story.setViewCount(story.getViewCount() + 1);
            storyRepository.save(story);
        }

        return ApiResponse.success(SuccessType.STORY_VIEWED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> getFeedStories(User currentUser) {
        List<Match> matches = matchRepository.findUserMatches(currentUser.getId());
        Set<Long> matchedUserIds = matches.stream()
                .map(m -> m.getUser1().getId().equals(currentUser.getId()) ? m.getUser2().getId() : m.getUser1().getId())
                .collect(Collectors.toSet());

        if (matchedUserIds.isEmpty()) {
             return ApiResponse.success(new ArrayList<>());
        }

        List<Story> activeStories = storyRepository.findActiveStoriesByUserIds(new ArrayList<>(matchedUserIds), LocalDateTime.now());

        Map<Long, List<Story>> storiesByUser = activeStories.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        List<UserStoryGroupDto> result = new ArrayList<>();

        for (Map.Entry<Long, List<Story>> entry : storiesByUser.entrySet()) {
            User storyUser = entry.getValue().get(0).getUser();
            boolean allSeen = true;
            List<StoryResponseDto> dtos = new ArrayList<>();

            for (Story s : entry.getValue()) {
                boolean isViewed = storyViewRepository.existsByStoryAndViewer(s, currentUser);
                if (!isViewed) allSeen = false;

                dtos.add(StoryResponseDto.builder()
                        .id(s.getId())
                        .mediaUrl(s.getMediaUrl())
                        .mediaType(s.getMediaType())
                        .createdAt(s.getCreatedAt())
                        .isViewed(isViewed)
                        .build());
            }

            result.add(UserStoryGroupDto.builder()
                    .userId(storyUser.getId())
                    .username(storyUser.getUsername())
                    .profilePic(storyUser.getProfilePic())
                    .stories(dtos)
                    .allSeen(allSeen)
                    .build());
        }

        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<?> getUserStories(Long targetUserId, User currentUser) {
        if (targetUserId.equals(currentUser.getId())) {
            return getMyStories(currentUser);
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        boolean isMatched = matchRepository.existsByUsers(currentUser, targetUser);

        List<Story> activeStories = storyRepository.findActiveStoriesByUser(targetUser, LocalDateTime.now());

        List<StoryResponseDto> dtos = new ArrayList<>();
        boolean allSeen = true;

        for (Story s : activeStories) {
            if (s.getStoryVisibility() == StoryVisibility.PRIVATE && !isMatched) {
                continue;
            }

            boolean isViewed = storyViewRepository.existsByStoryAndViewer(s, currentUser);
            if (!isViewed) allSeen = false;

            dtos.add(StoryResponseDto.builder()
                    .id(s.getId())
                    .mediaUrl(s.getMediaUrl())
                    .mediaType(s.getMediaType())
                    .createdAt(s.getCreatedAt())
                    .isViewed(isViewed)
                    .build());
        }

        if (dtos.isEmpty()) {
            return ApiResponse.success();
        }

        UserStoryGroupDto response = UserStoryGroupDto.builder()
                .userId(targetUser.getId())
                .username(targetUser.getUsername())
                .profilePic(targetUser.getProfilePic())
                .stories(dtos)
                .allSeen(allSeen)
                .build();

        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<?> getMyStories(User currentUser) {
        List<Story> activeStories = storyRepository.findActiveStoriesByUser(currentUser, LocalDateTime.now());
        List<StoryResponseDto> dtos = activeStories.stream().map(s -> {
            List<StoryView> views = storyViewRepository.findByStoryOrderByCreatedAtDesc(s);
            List<StoryViewerDto> viewers = views.stream().map(v -> StoryViewerDto.builder()
                    .userId(v.getViewer().getId())
                    .username(v.getViewer().getUsername())
                    .profilePic(v.getViewer().getProfilePic())
                    .viewedAt(v.getCreatedAt())
                    .build()).toList();

            return StoryResponseDto.builder()
                .id(s.getId())
                .mediaUrl(s.getMediaUrl())
                .mediaType(s.getMediaType())
                .createdAt(s.getCreatedAt())
                .isViewed(true)
                .viewCount(s.getViewCount())
                .viewers(viewers)
                .build();
        }).toList();

        return ApiResponse.success(dtos);
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteStory(Long storyId, User currentUser) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.STORY_NOT_FOUND));

        if (!story.getUser().getId().equals(currentUser.getId())) {
            throw RestException.restThrow(ErrorType.CANNOT_DELETE_OTHER_USERS_STORY);
        }

        story.setVisibility(false);
        storyRepository.save(story);

        return ApiResponse.success(SuccessType.STORY_DELETED_SUCCESSFULLY);
    }
}
