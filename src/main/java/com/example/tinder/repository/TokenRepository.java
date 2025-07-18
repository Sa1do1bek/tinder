package com.example.tinder.repository;

import com.example.tinder.entity.Token;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {

    List<Token> findByUserId(Long userId); // O'zgartirildi: Optional<Token> o'rniga List<Token>

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.user.id = :userId")
    void deleteByUserId(Long userId);
    @Query(value = "select * from token as t where t.token=?1",nativeQuery = true)
    Optional<Token> findByToken(String token);
}
