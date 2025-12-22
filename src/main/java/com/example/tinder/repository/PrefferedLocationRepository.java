package com.example.tinder.repository;

import com.example.tinder.entity.PreferredLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrefferedLocationRepository extends JpaRepository<PreferredLocation, Long> {

}
