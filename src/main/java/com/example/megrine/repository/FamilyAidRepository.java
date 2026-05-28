package com.example.megrine.repository;

import com.example.megrine.model.FamilyAid;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamilyAidRepository extends JpaRepository<FamilyAid, Long> {
    List<FamilyAid> findByFamilyIdOrderByAidDateDesc(Long familyId);
    List<FamilyAid> findTop10ByOrderByAidDateDesc();
}
