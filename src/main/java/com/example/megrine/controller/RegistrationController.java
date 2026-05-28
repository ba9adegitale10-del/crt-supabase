package com.example.megrine.controller;

import com.example.megrine.model.User;
import com.example.megrine.model.Volunteer;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.UserRepository;
import com.example.megrine.repository.VolunteerRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class RegistrationController {

    @Autowired private UserRepository userRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ActivityLogService logService;

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value="phone", required=false) String phone,
            @RequestParam(value="email", required=false) String email,
            @RequestParam(value="cin", required=false) String cin,
            @RequestParam(value="bloodType", required=false) String bloodType,
            @RequestParam(value="address", required=false) String address,
            @RequestParam(value="registrationNote", required=false) String note,
            RedirectAttributes ra) {

        // Validations
        String cleanUsername = username.trim().toLowerCase();
        if (userRepo.existsByUsername(cleanUsername)) {
            ra.addFlashAttribute("error", "Ce nom d'utilisateur est deja utilise. Choisissez un autre.");
            return "redirect:/register";
        }
        if (cleanUsername.length() < 3) {
            ra.addFlashAttribute("error", "Nom d'utilisateur trop court (min 3 caracteres).");
            return "redirect:/register";
        }
        if (!cleanUsername.matches("[a-zA-Z0-9_]+")) {
            ra.addFlashAttribute("error", "Nom d'utilisateur invalide. Utilisez uniquement lettres, chiffres et _");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            ra.addFlashAttribute("error", "Mot de passe trop court (min 6 caracteres).");
            return "redirect:/register";
        }
        if (firstName.isBlank() || lastName.isBlank()) {
            ra.addFlashAttribute("error", "Prenom et nom sont obligatoires.");
            return "redirect:/register";
        }

        // 1. Creer le profil Benevole (statut PENDING, inactif)
        Volunteer vol = new Volunteer();
        vol.setFirstName(firstName.trim());
        vol.setLastName(lastName.trim());
        vol.setEmail(email);
        vol.setPhone(phone);
        vol.setCin(cin);
        vol.setAddress(address);
        vol.setBloodType((bloodType != null && !bloodType.isBlank()) ? bloodType : null);
        vol.setJoinDate(LocalDate.now());
        vol.setActive(false); // Inactif jusqu'a approbation
        vol.setStatus(Volunteer.VolunteerStatus.PENDING);
        vol.setTotalHours(0);
        vol.setPoints(0);
        vol.setBadges("");
        vol.setAvailability("");
        Volunteer savedVol = volunteerRepo.save(vol);

        // 2. Creer le compte User lie au benevole
        User user = new User();
        user.setUsername(cleanUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(firstName.trim() + " " + lastName.trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setCin(cin);
        user.setBloodType(bloodType);
        user.setAddress(address);
        user.setRole("ROLE_MEMBER");
        user.setEnabled(false); // Desactive jusqu'a approbation
        user.setAccountStatus(User.AccountStatus.PENDING);
        user.setPoints(0);
        user.setRegisteredAt(LocalDateTime.now());
        user.setRegistrationNote(note);
        user.setPermissions(""); // Vide = acces complet (benevoles, events, formations, mon espace)
        user.setVolunteer(savedVol); // Lien direct
        userRepo.save(user);

        logService.log("Nouvelle demande inscription: " + cleanUsername,
            ActivityLog.ActionType.CREATE, "Inscription", cleanUsername,
            "Nom: " + firstName + " " + lastName +
            " | Tel: " + phone + " | Email: " + email +
            " | Groupe sang: " + bloodType + " | CIN: " + cin);

        return "redirect:/register?sent=true";
    }

    @GetMapping("/admin/registrations")
    public String pendingList(Model model) {
        java.util.List<User> pendingList = userRepo.findByAccountStatus(User.AccountStatus.PENDING);
        model.addAttribute("pending", pendingList);
        model.addAttribute("pendingCount", (long) pendingList.size());
        model.addAttribute("rejected", userRepo.findByAccountStatus(User.AccountStatus.REJECTED));
        return "admin/registrations";
    }

    @PostMapping("/admin/registrations/approve/{id}")
    public String approve(@PathVariable Long id,
            org.springframework.security.core.Authentication auth,
            RedirectAttributes ra) {
        try {
            User user = userRepo.findById(id).orElseThrow();

            // Activer le compte
            user.setEnabled(true);
            user.setAccountStatus(User.AccountStatus.ACTIVE);
            user.setApprovedAt(LocalDateTime.now());
            user.setApprovedBy(auth.getName());
            userRepo.save(user);

            // Activer le profil benevole automatiquement
            if (user.getVolunteer() != null) {
                Volunteer vol = user.getVolunteer();
                vol.setActive(true);
                vol.setStatus(Volunteer.VolunteerStatus.ACTIVE);
                volunteerRepo.save(vol);
            }

            logService.log("Inscription approuvee: " + user.getUsername(),
                ActivityLog.ActionType.UPDATE, "Inscription", user.getUsername(),
                "Approuve par: " + auth.getName() +
                " | Benevole active: " + user.getFullName());

            ra.addFlashAttribute("success",
                user.getFullName() + " est maintenant membre actif et benevole enregistre !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    @PostMapping("/admin/registrations/reject/{id}")
    public String reject(@PathVariable Long id,
            org.springframework.security.core.Authentication auth,
            RedirectAttributes ra) {
        try {
            User user = userRepo.findById(id).orElseThrow();
            user.setAccountStatus(User.AccountStatus.REJECTED);
            user.setApprovedBy(auth.getName());
            userRepo.save(user);

            if (user.getVolunteer() != null) {
                user.getVolunteer().setActive(false);
                volunteerRepo.save(user.getVolunteer());
            }

            logService.log("Inscription refusee: " + user.getUsername(),
                ActivityLog.ActionType.UPDATE, "Inscription", user.getUsername(),
                "Refuse par: " + auth.getName());

            ra.addFlashAttribute("success", "Demande refusee.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }
}
