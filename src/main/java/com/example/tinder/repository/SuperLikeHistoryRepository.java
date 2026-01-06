package com.example.tinder.repository;

import com.example.tinder.entity.SuperLikeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuperLikeHistoryRepository extends JpaRepository<SuperLikeHistory, Long> {
    
    @Query("""
        SELECT h FROM SuperLikeHistory h
        WHERE h.sender.id = :senderId
        AND h.receiver.id = :receiverId
        AND h.visibility = true
    """)
    Optional<SuperLikeHistory> findBySenderAndReceiver(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId);
    
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    
    @Query("""
        SELECT h FROM SuperLikeHistory h
        WHERE h.receiver.id = :receiverId
        AND h.visibility = true
        ORDER BY h.createdAt DESC
    """)
    Page<SuperLikeHistory> findAllReceivedSuperLikes(
            @Param("receiverId") Long receiverId, 
            Pageable pageable);
    
    @Query("""
        SELECT h FROM SuperLikeHistory h
        WHERE h.sender.id = :senderId
        AND h.visibility = true
        ORDER BY h.createdAt DESC
    """)
    Page<SuperLikeHistory> findAllSentSuperLikes(
            @Param("senderId") Long senderId, 
            Pageable pageable);
    
    @Query("""
        SELECT COUNT(h) FROM SuperLikeHistory h
        WHERE h.receiver.id = :userId
        AND h.isRewardPaid = false
        AND h.visibility = true
    """)
    Long countUnpaidRewards(@Param("userId") Long userId);
    
    @Query("""
        SELECT COALESCE(SUM(h.rewardAmount), 0.0) FROM SuperLikeHistory h
        WHERE h.receiver.id = :userId
        AND h.isRewardPaid = true
        AND h.visibility = true
    """)
    Double calculateTotalEarnings(@Param("userId") Long userId);
    
    @Query("""
        SELECT COUNT(h) FROM SuperLikeHistory h
        WHERE h.sender.id = :userId
        AND h.visibility = true
    """)
    Long countSentSuperLikes(@Param("userId") Long userId);
    
    @Query("""
        SELECT COUNT(h) FROM SuperLikeHistory h
        WHERE h.receiver.id = :userId
        AND h.visibility = true
    """)
    Long countReceivedSuperLikes(@Param("userId") Long userId);
}