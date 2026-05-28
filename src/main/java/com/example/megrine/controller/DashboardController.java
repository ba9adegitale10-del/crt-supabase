package com.example.megrine.controller;

import com.example.megrine.model.Event;
import com.example.megrine.model.Family;
import com.example.megrine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.math.BigDecimal;
import java.util.Collections;

@Controller
public class DashboardController {

    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private DonationRepository donationRepo;
    @Autowired private StockItemRepository stockRepo;
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

        // Chaque stat protegee contre les erreurs
        try { model.addAttribute("totalVolunteers", volunteerRepo.count()); }
        catch (Exception e) { model.addAttribute("totalVolunteers", 0); }

        try { model.addAttribute("activeVolunteers", volunteerRepo.countByActiveTrue()); }
        catch (Exception e) { model.addAttribute("activeVolunteers", 0); }

        try { model.addAttribute("totalDonations", donationRepo.count()); }
        catch (Exception e) { model.addAttribute("totalDonations", 0); }

        try {
            BigDecimal amount = donationRepo.sumMonetaryDonations();
            model.addAttribute("totalAmount", amount != null ? amount : BigDecimal.ZERO);
        } catch (Exception e) { model.addAttribute("totalAmount", BigDecimal.ZERO); }

        try { model.addAttribute("totalStock", stockRepo.count()); }
        catch (Exception e) { model.addAttribute("totalStock", 0); }

        try { model.addAttribute("totalFamilies", familyRepo.count()); }
        catch (Exception e) { model.addAttribute("totalFamilies", 0); }

        try { model.addAttribute("activeFamily", familyRepo.countByStatus(Family.FamilyStatus.ACTIVE)); }
        catch (Exception e) { model.addAttribute("activeFamily", 0); }

        try { model.addAttribute("totalEvents", eventRepo.count()); }
        catch (Exception e) { model.addAttribute("totalEvents", 0); }

        try { model.addAttribute("upcomingEvents", eventRepo.countByStatus(Event.EventStatus.PLANNED)); }
        catch (Exception e) { model.addAttribute("upcomingEvents", 0); }

        try { model.addAttribute("totalUsers", userRepo.count()); }
        catch (Exception e) { model.addAttribute("totalUsers", 0); }

        // Listes
        try { model.addAttribute("lowStock", stockRepo.findLowStock()); }
        catch (Exception e) { model.addAttribute("lowStock", Collections.emptyList()); }

        try {
            model.addAttribute("upcomingEventsList",
                eventRepo.findByStatusOrderByEventDateAsc(Event.EventStatus.PLANNED));
        } catch (Exception e) { model.addAttribute("upcomingEventsList", Collections.emptyList()); }

        try {
            model.addAttribute("topVolunteers",
                volunteerRepo.findByActiveTrue().stream().limit(5).toList());
        } catch (Exception e) { model.addAttribute("topVolunteers", Collections.emptyList()); }

        return "dashboard/index";
    }
}
