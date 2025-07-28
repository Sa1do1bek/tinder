package com.example.parking.repository;

import com.example.parking.entity.TempCarData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempCarDataRepository extends JpaRepository<TempCarData, Long> {
}
