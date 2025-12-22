package com.example.tinder.repository;

import com.example.tinder.entity.Preference;
import com.example.tinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.expression.spel.ast.OpAnd;

import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<Preference, Long>{

    @Query(value = """
                SELECT p FROM Preference p 
                WHERE p.user.id = :userId 
                AND p.visibility = true
            """)
    Optional<Preference> findByUserId(@Param("userId") Long userId);
}
