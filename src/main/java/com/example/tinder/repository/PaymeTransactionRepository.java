package com.example.tinder.repository;

import com.example.tinder.entity.PaymeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymeTransactionRepository extends JpaRepository<PaymeTransaction, String> {

    boolean existsByUserId(Long userId);

    List<PaymeTransaction> findAllByCreateTimeIsBetween(Long from, Long to);
}