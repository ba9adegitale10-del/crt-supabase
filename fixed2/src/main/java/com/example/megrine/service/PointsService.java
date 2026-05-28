package com.example.megrine.service;

import com.example.megrine.model.User;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.UserRepository;
import com.example.megrine.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PointsService {

    @Autowired private UserRepository userRepo;
    @Autowired private ActivityLogRepository logRepo;

    // Points par action
    public static final int POINTS_INSCRIPTION_EVENT = 10;
    public static final int POINTS_PARTICIPATION_COMPLETE = 50;
    public static final int POINTS_CONNEXION = 2;
    public static final int POINTS_PROFIL_COMPLET = 20;

    public void addPoints(String username, int points, String raison) {
        try {
            User user = userRepo.findByUsername(username).orElse(null);
            if (user == null || !user.getRole().equals("ROLE_MEMBER")) return;

            int ancienPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(ancienPoints + points);
            userRepo.save(user);

            // Logger le gain de points
            ActivityLog log = new ActivityLog();
            log.setUsername(username);
            log.setAction("+" + points + " points: " + raison);
            log.setActionType(ActivityLog.ActionType.UPDATE);
            log.setEntityType("Points");
            log.setEntityName(username);
            log.setDetails("Avant: " + ancienPoints + " | Apres: " + user.getPoints() + " | Badge: " + user.getBadgeLabel());
            log.setCreatedAt(LocalDateTime.now());
            logRepo.save(log);
        } catch (Exception e) { /* silent */ }
    }

    public void removePoints(String username, int points, String raison) {
        try {
            User user = userRepo.findByUsername(username).orElse(null);
            if (user == null) return;
            int ancienPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(Math.max(0, ancienPoints - points));
            userRepo.save(user);
        } catch (Exception e) { /* silent */ }
    }
}
