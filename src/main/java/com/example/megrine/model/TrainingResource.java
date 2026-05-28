package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "training_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String url;           // Lien externe ou chemin fichier
    private String fileType;      // PDF, VIDEO, LINK

    @Enumerated(EnumType.STRING)
    private ResourceCategory category = ResourceCategory.GENERAL;

    private LocalDate uploadedAt;
    private String uploadedBy;    // username de l'admin
    private boolean visible = true;

    public enum ResourceCategory {
        GENERAL, PREMIERS_SECOURS, FORMATION, SECURITE, PROTOCOLE, AUTRE
    }
}
