package com.example.megrine.repository;

import com.example.megrine.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.type = 'MONETARY'")
    BigDecimal sumMonetaryDonations();
    long count();
}
