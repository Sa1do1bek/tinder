package com.example.tinder.repository;

import com.example.tinder.entity.Swipe;
import com.example.tinder.enums.SwipeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {

  boolean existsBySwiperIdAndSwipedIdAndSwipe(Long swipedId, Long swiperId, SwipeStatus swipe);

  List<Swipe> findAllBySwiper_Id(Long userId);

  Optional<Swipe> findBySwiperIdAndSwipedId(Long swiperId, Long swipedId);

}