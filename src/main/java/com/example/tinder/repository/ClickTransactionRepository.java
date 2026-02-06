package com.example.tinder.repository;

import com.example.tinder.entity.ClickTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClickTransactionRepository extends JpaRepository<ClickTransaction, Long> {
}
