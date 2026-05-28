package com.example.megrine.repository;

import com.example.megrine.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByStockItemIdOrderByMovedAtDesc(Long itemId);
    List<StockMovement> findTop20ByOrderByMovedAtDesc();
}
