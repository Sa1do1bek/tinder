package com.example.tinder.repository;

import com.example.tinder.entity.PayMeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayMeTransactionRepository extends JpaRepository<PayMeTransaction, Long> {

    boolean existsByPaymeTransId(String paymeTransId);

    Optional<PayMeTransaction> findByPaymeTransId(String paymeTransId);

}
