package com.example.megrine.service;

import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository logRepo;

    public void log(String action, ActivityLog.ActionType type,
                    String entityType, String entityName, String details) {
        try {
            ActivityLog log = new ActivityLog();
            log.setAction(action);
            log.setActionType(type);
            log.setEntityType(entityType);
            log.setEntityName(entityName);
            log.setDetails(details);
            log.setCreatedAt(LocalDateTime.now());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                !auth.getName().equals("anonymousUser")) {
                log.setUsername(auth.getName());
            } else {
                log.setUsername("Systeme");
            }
            logRepo.save(log);
        } catch (Exception e) {
            // Silent fail
        }
    }

    public void logCreate(String entityType, String entityName) {
        log("Ajout : " + entityName, ActivityLog.ActionType.CREATE, entityType, entityName, null);
    }

    public void logUpdate(String entityType, String entityName) {
        log("Modification : " + entityName, ActivityLog.ActionType.UPDATE, entityType, entityName, null);
    }

    public void logDelete(String entityType, String entityName) {
        log("Suppression : " + entityName, ActivityLog.ActionType.DELETE, entityType, entityName, null);
    }

    public void logLogin(String username) {
        ActivityLog log = new ActivityLog();
        log.setUsername(username);
        log.setAction("Connexion au systeme");
        log.setActionType(ActivityLog.ActionType.LOGIN);
        log.setEntityType("Systeme");
        log.setEntityName("Session");
        log.setCreatedAt(LocalDateTime.now());
        logRepo.save(log);
    }
}
