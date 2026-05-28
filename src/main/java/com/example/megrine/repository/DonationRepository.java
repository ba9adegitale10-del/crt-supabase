package com.example.megrine.repository;

import com.example.megrine.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    // nativeQuery=true pour eviter les problemes JPQL avec les enums
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM donations WHERE type = 'MONETARY'",
           nativeQuery = true)
    BigDecimal sumMonetaryDonations();
}
