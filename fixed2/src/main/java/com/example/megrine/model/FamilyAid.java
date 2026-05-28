package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "family_aids")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyAid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    private String aidType; // Type d'aide
    private String description;
    private BigDecimal amount;
    private Integer quantity;
    private String unit;
    private LocalDate aidDate;
    private String givenBy; // Responsable
    private String notes;

    @Enumerated(EnumType.STRING)
    private AidType type = AidType.FOOD;

    public enum AidType {
        FOOD, MONEY, CLOTHES, MEDICAL, SCHOOL, OTHER
    }
}
