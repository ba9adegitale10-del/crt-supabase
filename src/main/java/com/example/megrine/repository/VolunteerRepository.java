package com.example.megrine.repository;

import com.example.megrine.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    List<Volunteer> findByActiveTrue();
    long countByActiveTrue();
    List<Volunteer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
