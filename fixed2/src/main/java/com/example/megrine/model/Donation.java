package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String donorName;
    private String donorEmail;
    private String donorPhone;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private DonationType type = DonationType.MONETARY;

    private String description;
    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    private DonationStatus status = DonationStatus.RECEIVED;

    public enum DonationType {
        MONETARY, FOOD, CLOTHES, MEDICAL, OTHER
    }

    public enum DonationStatus {
        RECEIVED, PENDING, DISTRIBUTED
    }
}
