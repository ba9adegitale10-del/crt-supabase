package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_participations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "volunteer_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    private LocalDateTime enrolledAt;
    private Integer hoursContributed = 0;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status = ParticipationStatus.ENROLLED;

    public enum ParticipationStatus {
        ENROLLED, CONFIRMED, COMPLETED, CANCELLED
    }
}
