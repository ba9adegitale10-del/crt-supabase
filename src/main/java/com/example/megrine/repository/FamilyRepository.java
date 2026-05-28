package com.example.megrine.repository;

import com.example.megrine.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    List<Family> findByCategory(Family.FamilyCategory category);
    List<Family> findByStatus(Family.FamilyStatus status);
    List<Family> findByHeadNameContainingIgnoreCase(String name);
    long countByStatus(Family.FamilyStatus status);
}
