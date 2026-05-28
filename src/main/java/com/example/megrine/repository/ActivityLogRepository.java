package com.example.megrine.repository;

import com.example.megrine.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop100ByOrderByCreatedAtDesc();
    List<ActivityLog> findByUsernameOrderByCreatedAtDesc(String username);
    List<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    List<ActivityLog> findByActionTypeOrderByCreatedAtDesc(ActivityLog.ActionType actionType);

    @Query("SELECT a.username, COUNT(a) FROM ActivityLog a GROUP BY a.username ORDER BY COUNT(a) DESC")
    List<Object[]> countByUser();

    @Query("SELECT a.entityType, COUNT(a) FROM ActivityLog a GROUP BY a.entityType ORDER BY COUNT(a) DESC")
    List<Object[]> countByEntityType();

    long countByActionType(ActivityLog.ActionType type);
}
