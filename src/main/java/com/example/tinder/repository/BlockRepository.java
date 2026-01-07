package com.example.tinder.repository;

import com.example.tinder.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    @Query("""
        select b from Block b
        where b.blocker.id = :blockerId
        and b.blocked.id = :blockedId
        and b.visibility = true
    """)
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("""
        select b from Block b
        where b.blocker.id = :blockerId
        and b.blocked.id = :blockedId
        and b.visibility = true
    """)
    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("""
        select b from Block b
        where b.blocker.id = :userId
        and b.visibility = true
    """)
    List<Block> findAllUserBlocks(@Param("userId") Long userId);
}