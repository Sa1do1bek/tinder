package com.example.parking.repository;

import com.example.parking.entity.TempOilChangeData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempOilChangeDataRepository extends JpaRepository<TempOilChangeData, Long> {
}