package com.example.tinder.repository;

import com.example.tinder.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<Code,Long> {
    Optional<Code> findByUserIdAndDeviceId(Long id, Long deviceId);

    Optional<Code> findByUserId(Long id);
    // User'ning barcha Code'larini olish
    List<Code> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Eng oxirgi Code'ni olish
    @Query("SELECT c FROM Code c WHERE c.user.id = ?1 ORDER BY c.createdAt DESC LIMIT 1")
    Optional<Code> findLatestByUserId(Long userId);

    // Device bo'yicha Code'larni olish
    List<Code> findByDeviceId(Long deviceId);


    @Query(value = """
        SELECT c FROM Code c
        WHERE c.code = :code  \s
   \s""")
    void deleteByCode(@Param("code") String code);
}
