package com.example.megrine.controller;

import com.example.megrine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/database")
public class DatabaseController {

    @Autowired private UserRepository userRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private DonationRepository donationRepo;
    @Autowired private StockItemRepository stockRepo;
    @Autowired private StockMovementRepository movementRepo;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("volunteers", volunteerRepo.findAll());
        model.addAttribute("donations", donationRepo.findAll());
        model.addAttribute("stockItems", stockRepo.findAll());
        model.addAttribute("movements", movementRepo.findTop20ByOrderByMovedAtDesc());
        model.addAttribute("stats", new long[]{
            userRepo.count(),
            volunteerRepo.count(),
            donationRepo.count(),
            stockRepo.count()
        });
        return "database/view";
    }
}
