package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String location;
    private LocalDate eventDate;

    private String responsibleName; // Responsable de l'action
    private String volunteerNames;  // Noms des bénévoles (texte libre)
    private String familyNames;     // Noms des familles bénéficiaires
    private Integer participantsCount;
    private String notes;

    @Enumerated(EnumType.STRING)
    private EventType type = EventType.HUMANITARIAN;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.UPCOMING;

    public enum EventType {
        HUMANITARIAN, BLOOD_DONATION, TRAINING, AWARENESS, DISTRIBUTION, OTHER
    }

    public enum EventStatus {
        UPCOMING, ONGOING, COMPLETED, CANCELLED
    }
}
