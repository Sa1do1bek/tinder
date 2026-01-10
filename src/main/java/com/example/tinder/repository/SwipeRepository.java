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

  boolean existsBySwiperIdAndSwipedIdAndSwipeAndVisibilityTrue(Long swipedId, Long swiperId, SwipeStatus swipe);

  List<Swipe> findAllBySwiper_Id(Long userId);

  Optional<Swipe> findBySwiperIdAndSwipedId(Long swiperId, Long swipedId);

  @Query("""
        SELECT s.swiped FROM Swipe s
        WHERE s.swiper.id = :userId AND s.swipe = com.example.tinder.enums.SwipeStatus.REJECT
        AND s.visibility = true
    """)
  Page<User> findAllRejectedUsers(@Param("userId") Long userId, Pageable pageable);

  /**
   * LIKE va SUPERLIKE larni aralash olib kelish
   */
  @Query("""
        SELECT s.swiped FROM Swipe s
        WHERE s.swiper.id = :userId
        AND (s.swipe = com.example.tinder.enums.SwipeStatus.LIKE 
             OR s.swipe = com.example.tinder.enums.SwipeStatus.SUPERLIKE)
        AND s.visibility = true
        ORDER BY s.createdAt DESC
    """)
  Page<User> findAllLikeUsers(@Param("userId") Long userId, Pageable pageable);

  @Modifying
  @Transactional
  @Query(value = """
        DELETE FROM swipe
        WHERE swipe = 'REJECT'
        AND created_at <= NOW() - INTERVAL '5 minutes'
        """, nativeQuery = true)
  void deleteExpiredRejects();

  /**
   * Menga LIKE yoki SUPERLIKE bosgan userlarni topish
   */
  @Query("""
        SELECT s.swiper FROM Swipe s
        WHERE s.swiped.id = :userId
        AND (s.swipe = com.example.tinder.enums.SwipeStatus.LIKE
             OR s.swipe = com.example.tinder.enums.SwipeStatus.SUPERLIKE)
        AND s.visibility = true
        ORDER BY s.createdAt DESC
    """)
  Page<User> findAllUsersWhoLikeMe(@Param("userId") Long userId, Pageable pageable);

  @Transactional
  @Modifying
  @Query("""
        UPDATE Swipe s
        SET s.visibility = false
        WHERE
          (s.swiper.id = :u1 AND s.swiped.id = :u2)
          OR
          (s.swiper.id = :u2 AND s.swiped.id = :u1)
    """)
  void hideSwipes(Long u1, Long u2);

  /**
   * Bitta user uchun swipe type ni topish
   */
  @Query("""
        SELECT s.swipe FROM Swipe s
        WHERE s.swiper.id = :swiperId
        AND s.swiped.id = :swipedId
        AND s.visibility = true
    """)
  Optional<SwipeStatus> findSwipeTypeByUsers(@Param("swiperId") Long swiperId,
                                             @Param("swipedId") Long swipedId);
}