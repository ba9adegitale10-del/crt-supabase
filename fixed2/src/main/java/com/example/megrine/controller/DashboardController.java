package com.example.megrine.controller;

import com.example.megrine.model.Event;
import com.example.megrine.model.User;
import com.example.megrine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private DonationRepository donationRepo;
    @Autowired private StockItemRepository stockRepo;
    @Autowired private StockMovementRepository movementRepo;
    @Autowired private FamilyRepository familyRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            org.springframework.security.core.Authentication auth) {
        // Rediriger les membres vers Mon Espace
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER"))
            && auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            && auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return "redirect:/member";
        }
        model.addAttribute("totalVolunteers", volunteerRepo.count());
        model.addAttribute("activeVolunteers", volunteerRepo.countByActiveTrue());
        model.addAttribute("totalDonations", donationRepo.count());
        model.addAttribute("totalAmount", donationRepo.sumMonetaryDonations());
        model.addAttribute("totalStockItems", stockRepo.count());
        model.addAttribute("lowStockCount", stockRepo.findLowStock().size());
        model.addAttribute("totalFamilies", familyRepo.count());
        model.addAttribute("activeFamilies", familyRepo.countByStatus(com.example.megrine.model.Family.FamilyStatus.ACTIVE));
        model.addAttribute("upcomingEvents", eventRepo.countByStatus(Event.EventStatus.UPCOMING));
        model.addAttribute("lowStockItems", stockRepo.findLowStock());
        model.addAttribute("recentEvents", eventRepo.findByStatusOrderByEventDateAsc(Event.EventStatus.UPCOMING));
        model.addAttribute("volunteers", volunteerRepo.findAll().stream().limit(5).toList());
        return "dashboard/index";
    }
}
