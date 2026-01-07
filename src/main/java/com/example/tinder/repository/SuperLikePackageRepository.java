package com.example.tinder.repository;

import com.example.tinder.entity.SuperLikePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuperLikePackageRepository extends JpaRepository<SuperLikePackage, Long> {
    
    List<SuperLikePackage> findAllByVisibilityTrueOrderByQuantityAsc();

    @Query("""
        SELECT p FROM SuperLikePackage p
        WHERE p.id = :id
        AND p.visibility = true
    """)
    Optional<SuperLikePackage> findByIdAndVisibilityTrue(Long id);
    
    @Query("""
        SELECT p FROM SuperLikePackage p
        WHERE p.visibility = true
        ORDER BY p.quantity ASC
    """)
    List<SuperLikePackage> findAllActivePackages();
}