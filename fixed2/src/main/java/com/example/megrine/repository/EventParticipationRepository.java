package com.example.megrine.repository;

import com.example.megrine.model.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {
    // Verifier si un benevole est deja inscrit
    Optional<EventParticipation> findByEventIdAndVolunteerId(Long eventId, Long volunteerId);
    boolean existsByEventIdAndVolunteerId(Long eventId, Long volunteerId);
    // Toutes les participations d'un benevole
    List<EventParticipation> findByVolunteerIdOrderByEnrolledAtDesc(Long volunteerId);
    // Tous les inscrits d'un evenement
    List<EventParticipation> findByEventIdOrderByEnrolledAtAsc(Long eventId);
    // Compter inscrits d'un evenement
    long countByEventId(Long eventId);
    // Somme heures d'un benevole
    @Query("SELECT COALESCE(SUM(p.hoursContributed),0) FROM EventParticipation p WHERE p.volunteer.id = :vid AND p.status = 'COMPLETED'")
    Integer sumHoursByVolunteerId(Long vid);
}
