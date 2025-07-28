package com.example.parking.repository;

import com.example.parking.entity.OilChangeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OilChangeInfoRepository extends JpaRepository<OilChangeInfo, Long> {
    
    @Query("SELECT o FROM OilChangeInfo o WHERE o.user.chatId = :chatId ORDER BY o.lastChangeDate DESC")
    List<OilChangeInfo> findByUserChatIdOrderByLastChangeDateDesc(@Param("chatId") Long chatId);
    
    @Query("SELECT o FROM OilChangeInfo o WHERE o.user.chatId = :chatId ORDER BY o.lastChangeDate DESC")
    Optional<OilChangeInfo> findTopByUserChatIdOrderByLastChangeDateDesc(@Param("chatId") Long chatId);
    
    @Query("SELECT o FROM OilChangeInfo o WHERE o.user.chatId = :chatId")
    List<OilChangeInfo> findByUserChatId(@Param("chatId") Long chatId);
    
    @Query("SELECT o FROM OilChangeInfo o WHERE o.nextChangeDate <= :date")
    List<OilChangeInfo> findByNextChangeDateLessThanEqual(@Param("date") LocalDate date);
}