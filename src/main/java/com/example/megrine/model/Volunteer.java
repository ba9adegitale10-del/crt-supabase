package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "volunteers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String email;
    private String phone;
    private String bloodType;
    private String address;
    private String cin;
    private Integer points = 0;
    private LocalDate joinDate;
    private boolean active = true;

    // Heures de benevol at et badges
    private Integer totalHours = 0;
    private String badges = ""; // JSON-like: "BRONZE,SILVER"

    // Disponibilites (CSV): "WEEKEND,SOIREE,MATIN"
    @Column(length = 500)
    private String availability = "";

    @Enumerated(EnumType.STRING)
    private VolunteerStatus status = VolunteerStatus.ACTIVE;

    public enum VolunteerStatus {
        ACTIVE, INACTIVE, PENDING
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Calcul badge automatique selon heures
    public String getComputedBadge() {
        if (totalHours == null || totalHours < 10) return "DEBUTANT";
        if (totalHours < 50)  return "BRONZE";
        if (totalHours < 150) return "ARGENT";
        if (totalHours < 500) return "OR";
        return "PLATINE";
    }
}
