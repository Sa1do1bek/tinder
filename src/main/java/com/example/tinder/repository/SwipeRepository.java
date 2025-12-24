package com.example.tinder.repository;

import com.example.tinder.entity.Swipe;
import com.example.tinder.entity.User;
import com.example.tinder.enums.SwipeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {

  boolean existsBySwiperIdAndSwipedIdAndSwipe(Long swipedId, Long swiperId, SwipeStatus swipe);

  List<Swipe> findAllBySwiper_Id(Long userId);

  Optional<Swipe> findBySwiperIdAndSwipedId(Long swiperId, Long swipedId);
    @Query("""
    SELECT s.swiped FROM Swipe s
    WHERE s.swiper.id = :userId AND s.swipe = com.example.tinder.enums.SwipeStatus.REJECT
""")
    Page<User> findAllRejectedUsers(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT s.swiped FROM Swipe s
    WHERE s.swiper.id = :userId 
    AND s.swipe = com.example.tinder.enums.SwipeStatus.ACCEPT
""")
    Page<User> findAllAcceptedUsers(@Param("userId") Long userId, Pageable pageable);


  @Modifying
  @Transactional
  @Query(value = """
        DELETE FROM swipe
        WHERE swipe = com.example.tinder.enums.SwipeStatus.REJECT
        AND created_at <= NOW() - INTERVAL '5 minutes'
        """, nativeQuery = true)
  void deleteExpiredRejects();

  @Query("""
    SELECT s.swiper FROM Swipe s
    WHERE s.swiped.id = :userId
    AND s.swipe = com.example.tinder.enums.SwipeStatus.ACCEPT
""")
  Page<User> findAllUsersWhoLikeMe(@Param("userId") Long userId, Pageable pageable);
}