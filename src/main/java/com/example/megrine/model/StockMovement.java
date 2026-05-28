package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private Integer quantity;
    private String reason;
    private String performedBy;
    private LocalDateTime movedAt;

    public enum MovementType {
        IN, OUT
    }
}
