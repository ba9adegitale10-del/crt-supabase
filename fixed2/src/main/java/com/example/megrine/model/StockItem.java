package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "stock_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;
    private String unit; // kg, litres, pièces, boîtes...

    @Column(nullable = false)
    private Integer quantity = 0;

    private Integer quantityMin = 0; // seuil d'alerte
    private String description;
    private String location; // emplacement dans l'entrepôt
    private LocalDate lastUpdated;

    @Enumerated(EnumType.STRING)
    private StockCategory categoryEnum = StockCategory.OTHER;

    public enum StockCategory {
        FOOD, CLOTHES, MEDICAL, HYGIENE, EQUIPMENT, OTHER
    }

    public boolean isLow() {
        return quantityMin != null && quantity <= quantityMin;
    }
}
