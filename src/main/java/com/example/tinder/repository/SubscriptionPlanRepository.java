package com.example.tinder.repository;

import com.example.tinder.entity.SubscriptionPlan;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    @Query(value = """
                SELECT sp FROM SubscriptionPlan sp
                WHERE sp.code = :code
                AND sp.visibility = true
            """)
    Optional<SubscriptionPlan> findByCode(@Param("code") String code);

    @NotNull
    @Query(value = """
                SELECT sp FROM SubscriptionPlan sp
                WHERE sp.visibility = true
                ORDER BY sp.months ASC
            """)
    List<SubscriptionPlan> findAll();


    @NotNull
    @Query(value = """
                SELECT sp FROM SubscriptionPlan sp
                WHERE sp.id = :id
                AND sp.visibility = true
            """)
    Optional<SubscriptionPlan> findById(@NotNull @Param("id") Long id);

}
