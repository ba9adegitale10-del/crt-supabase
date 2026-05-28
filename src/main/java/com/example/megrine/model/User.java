package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_USER, ROLE_MEMBER, ROLE_PENDING

    private String fullName;
    private String email;
    private String phone;
    private String cin;       // Carte identite
    private Integer age;
    private String bloodType; // Groupe sanguin
    private String address;
    private boolean enabled = true;

    // Lien profil benevole
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = true)
    private Volunteer volunteer;

    // Permissions granulaires
    @Column(length = 500)
    private String permissions = "";

    // Points systeme
    private Integer points = 0;

    // Inscription
    private LocalDateTime registeredAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String registrationNote; // Message du candidat

    // Statut demande
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    public enum AccountStatus {
        PENDING,   // En attente d'approbation
        ACTIVE,    // Approuve et actif
        REJECTED,  // Refuse
        SUSPENDED  // Suspendu
    }

    public boolean canAccess(String section) {
        if (!enabled) return false;
        if (role != null && role.equals("ROLE_ADMIN")) return true;
        if (permissions == null || permissions.isBlank()) return true;
        return permissions.contains(section.toUpperCase());
    }

    public String getBadgeLabel() {
        if (points == null) return "Nouveau";
        if (points < 50)   return "Nouveau";
        if (points < 150)  return "Bronze";
        if (points < 400)  return "Argent";
        if (points < 1000) return "Or";
        return "Platine";
    }

    public String getBadgeColor() {
        if (points == null || points < 50)  return "#6B7280";
        if (points < 150) return "#B45309";
        if (points < 400) return "#9CA3AF";
        if (points < 1000) return "#D97706";
        return "#7C3AED";
    }
}
