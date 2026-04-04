package com.example.tinder.repository;

import com.example.tinder.entity.Story;
import com.example.tinder.entity.StoryView;
import com.example.tinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
    boolean existsByStoryAndViewer(Story story, User viewer);
    List<StoryView> findByStoryOrderByCreatedAtDesc(Story story);
}
