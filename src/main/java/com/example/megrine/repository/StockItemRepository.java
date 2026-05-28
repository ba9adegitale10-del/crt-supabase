package com.example.megrine.repository;

import com.example.megrine.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    List<StockItem> findByCategoryEnum(StockItem.StockCategory category);
    List<StockItem> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM StockItem s WHERE s.quantityMin IS NOT NULL AND s.quantity <= s.quantityMin")
    List<StockItem> findLowStock();

    long countByQuantityGreaterThan(int qty);
}
