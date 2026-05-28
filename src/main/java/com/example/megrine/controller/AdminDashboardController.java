package com.example.megrine.controller;

import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("totalLogs",    logRepo.count());
        model.addAttribute("createCount",  logRepo.countByActionType(ActivityLog.ActionType.CREATE));
        model.addAttribute("updateCount",  logRepo.countByActionType(ActivityLog.ActionType.UPDATE));
        model.addAttribute("deleteCount",  logRepo.countByActionType(ActivityLog.ActionType.DELETE));
        model.addAttribute("loginCount",   logRepo.countByActionType(ActivityLog.ActionType.LOGIN));
        model.addAttribute("logoutCount",  logRepo.countByActionType(ActivityLog.ActionType.LOGOUT));
        model.addAttribute("userActivity", logRepo.countByUser());
        model.addAttribute("entityActivity", logRepo.countByEntityType());
        model.addAttribute("totalUsers",    userRepo.count());
        model.addAttribute("totalVolunteers", volunteerRepo.count());
        model.addAttribute("totalFamilies", familyRepo.count());
        model.addAttribute("totalDonations", donationRepo.count());
        model.addAttribute("totalAmount",  donationRepo.sumMonetaryDonations());
        model.addAttribute("totalStock",   stockRepo.count());
        model.addAttribute("totalEvents",  eventRepo.count());
        model.addAttribute("totalParticipations", participationRepo.count());
        model.addAttribute("pendingCount",
            userRepo.countByAccountStatus(com.example.megrine.model.User.AccountStatus.PENDING));
        model.addAttribute("recentLogins", logRepo.findByActionTypeOrderByCreatedAtDesc(ActivityLog.ActionType.LOGIN));
        model.addAttribute("pendingCount", userRepo.countByAccountStatus(com.example.megrine.model.User.AccountStatus.PENDING));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("logs", logRepo.findTop100ByOrderByCreatedAtDesc());
        addStats(model);
        return "admin/dashboard";
    }

    @GetMapping("/logs/filter")
    public String filterLogs(@RequestParam(required=false) String type,
            @RequestParam(required=false) String user, Model model) {
        if (user != null && !user.isBlank()) {
            model.addAttribute("logs", logRepo.findByUsernameOrderByCreatedAtDesc(user));
            model.addAttribute("filterUser", user);
        } else if (type != null && !type.isBlank()) {
            try {
                model.addAttribute("logs", logRepo.findByActionTypeOrderByCreatedAtDesc(ActivityLog.ActionType.valueOf(type)));
            } catch (Exception e) {
                model.addAttribute("logs", logRepo.findTop100ByOrderByCreatedAtDesc());
            }
            model.addAttribute("filterType", type);
        } else {
            model.addAttribute("logs", logRepo.findTop100ByOrderByCreatedAtDesc());
        }
        addStats(model);
        return "admin/dashboard";
    }
}
