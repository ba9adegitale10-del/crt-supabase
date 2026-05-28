package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "families")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Family {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String headName; // Nom du chef de famille

    private String phone;
    private String address;
    private String cin; // Carte d'identité
    private Integer membersCount; // Nombre de membres
    private String situation; // Veuf, divorcé, etc.
    private String notes;
    private LocalDate registeredDate;

    @Enumerated(EnumType.STRING)
    private FamilyCategory category = FamilyCategory.NEEDY;

    @Enumerated(EnumType.STRING)
    private FamilyStatus status = FamilyStatus.ACTIVE;

    public enum FamilyCategory {
        NEEDY,       // Nécessiteuse
        ORPHAN,      // Orphelins
        DISABLED,    // Handicapés
        ELDERLY,     // Personnes âgées
        REFUGEE,     // Réfugiés
        OTHER
    }

    public enum FamilyStatus {
        ACTIVE, INACTIVE, PENDING
    }
}
