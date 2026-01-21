package com.example.tinder.repository;

import com.example.tinder.entity.ClickTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClickTransactionRepository extends JpaRepository<ClickTransaction, Long> {

    boolean existsByClickTransId(String clickTransId);

    Optional<ClickTransaction> findByClickTransId(String clickTransId);
}
