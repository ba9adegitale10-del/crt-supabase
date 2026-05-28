package com.example.megrine.controller;

import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private ActivityLogRepository logRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private FamilyRepository familyRepo;
    @Autowired private DonationRepository donationRepo;
    @Autowired private StockItemRepository stockRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private EventParticipationRepository participationRepo;

    private void addStats(Model model) {
        // Chaque stat est protegee individuellement — une erreur n'arrete pas tout
        safeAdd(model, "totalLogs",    () -> logRepo.count());
        safeAdd(model, "createCount",  () -> logRepo.countByActionType(ActivityLog.ActionType.CREATE));
        safeAdd(model, "updateCount",  () -> logRepo.countByActionType(ActivityLog.ActionType.UPDATE));
        safeAdd(model, "deleteCount",  () -> logRepo.countByActionType(ActivityLog.ActionType.DELETE));
        safeAdd(model, "loginCount",   () -> logRepo.countByActionType(ActivityLog.ActionType.LOGIN));
        safeAdd(model, "logoutCount",  () -> logRepo.countByActionType(ActivityLog.ActionType.LOGOUT));
        safeAdd(model, "userActivity",   () -> logRepo.countByUser());
        safeAdd(model, "entityActivity", () -> logRepo.countByEntityType());
        safeAdd(model, "totalUsers",       () -> userRepo.count());
        safeAdd(model, "totalVolunteers",  () -> volunteerRepo.count());
        safeAdd(model, "totalFamilies",    () -> familyRepo.count());
        safeAdd(model, "totalDonations",   () -> donationRepo.count());
        safeAdd(model, "totalStock",       () -> stockRepo.count());
        safeAdd(model, "totalEvents",      () -> eventRepo.count());
        safeAdd(model, "totalParticipations", () -> participationRepo.count());

        // Montant monetaire — retourne 0 si probleme
        try {
            BigDecimal amount = donationRepo.sumMonetaryDonations();
            model.addAttribute("totalAmount", amount != null ? amount : BigDecimal.ZERO);
        } catch (Exception e) {
            model.addAttribute("totalAmount", BigDecimal.ZERO);
        }

        // Compter inscriptions en attente
        try {
            model.addAttribute("pendingCount",
                userRepo.countByAccountStatus(com.example.megrine.model.User.AccountStatus.PENDING));
        } catch (Exception e) {
            model.addAttribute("pendingCount", 0L);
        }

        // Derniers logins
        try {
            model.addAttribute("recentLogins",
                logRepo.findByActionTypeOrderByCreatedAtDesc(ActivityLog.ActionType.LOGIN));
        } catch (Exception e) {
            model.addAttribute("recentLogins", java.util.Collections.emptyList());
        }
    }

    /** Execute un supplier et met le resultat dans le model — jamais de 500 */
    private void safeAdd(Model model, String key, java.util.function.Supplier<?> supplier) {
        try {
            Object val = supplier.get();
            model.addAttribute(key, val != null ? val : 0);
        } catch (Exception e) {
            model.addAttribute(key, 0);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("logs", logRepo.findTop100ByOrderByCreatedAtDesc());
        } catch (Exception e) {
            model.addAttribute("logs", java.util.Collections.emptyList());
        }
        addStats(model);
        return "admin/dashboard";
    }

    @GetMapping("/logs/filter")
    public String filterLogs(@RequestParam(required = false) String type,
                             @RequestParam(required = false) String user, Model model) {
        try {
            if (user != null && !user.isBlank()) {
                model.addAttribute("logs", logRepo.findByUsernameOrderByCreatedAtDesc(user));
                model.addAttribute("filterUser", user);
            } else if (type != null && !type.isBlank()) {
                try {
                    ActivityLog.ActionType actionType = ActivityLog.ActionType.valueOf(type);
                    model.addAttribute("logs", logRepo.findByActionTypeOrderByCreatedAtDesc(actionType));
                } catch (IllegalArgumentException ex) {
                    model.addAttribute("logs", logRepo.findTop100ByOrderByCreatedAtDesc());
                }
                model.addAttribute("filterType", type);
            } else {
                model.addAttribute("logs", logRepo.findTop100ByOrderByCreatedAtDesc());
            }
        } catch (Exception e) {
            model.addAttribute("logs", java.util.Collections.emptyList());
        }
        addStats(model);
        return "admin/dashboard";
    }
}
