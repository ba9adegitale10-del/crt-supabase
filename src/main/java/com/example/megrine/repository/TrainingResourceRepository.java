package com.example.megrine.repository;

import com.example.megrine.model.TrainingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingResourceRepository extends JpaRepository<TrainingResource, Long> {
    List<TrainingResource> findByVisibleTrueOrderByUploadedAtDesc();
    List<TrainingResource> findByCategory(TrainingResource.ResourceCategory category);
}
