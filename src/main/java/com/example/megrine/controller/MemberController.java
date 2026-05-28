package com.example.megrine.controller;

import com.example.megrine.model.*;
import com.example.megrine.repository.*;
import com.example.megrine.service.ActivityLogService;
import com.example.megrine.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired private UserRepository userRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private EventParticipationRepository participationRepo;
    @Autowired private TrainingResourceRepository trainingRepo;
    @Autowired private ActivityLogService logService;
    @Autowired private PointsService pointsService;

    private User getCurrentUser(Authentication auth) {
        return userRepo.findByUsername(auth.getName()).orElseThrow();
    }

    // MON ESPACE - SECURITE: chaque membre voit UNIQUEMENT ses donnees
    @GetMapping
    public String monEspace(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("badgeLabel", user.getBadgeLabel());
        model.addAttribute("badgeColor", user.getBadgeColor());
        model.addAttribute("nextBadgePoints", getNextBadgePoints(user.getPoints()));

        // Leaderboard des membres (points)
        model.addAttribute("leaderboard", userRepo.findMembersByPoints().stream().limit(5).toList());

        if (user.getVolunteer() != null) {
            Volunteer vol = user.getVolunteer();
            model.addAttribute("volunteer", vol);

            Integer hours = participationRepo.sumHoursByVolunteerId(vol.getId());
            model.addAttribute("totalHours", hours != null ? hours : 0);

            // SES participations uniquement
            List<EventParticipation> participations =
                participationRepo.findByVolunteerIdOrderByEnrolledAtDesc(vol.getId());
            model.addAttribute("participations", participations);

            // IDs des events ou il est inscrit (pour afficher bouton "Inscrit")
            List<Long> enrolledEventIds = participations.stream()
                .filter(p -> p.getStatus() != EventParticipation.ParticipationStatus.CANCELLED)
                .map(p -> p.getEvent().getId())
                .toList();
            model.addAttribute("enrolledEventIds", enrolledEventIds);
        }

        // Evenements disponibles pour inscription
        model.addAttribute("upcomingEvents",
            eventRepo.findByStatusOrderByEventDateAsc(Event.EventStatus.UPCOMING));

        // Formations (lecture seule)
        model.addAttribute("resources", trainingRepo.findByVisibleTrueOrderByUploadedAtDesc());

        return "member/dashboard";
    }

    // Disponibilites avec whitelist
    @GetMapping("/availability")
    public String availabilityForm(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        if (user.getVolunteer() != null) {
            String avail = user.getVolunteer().getAvailability();
            List<String> selected = (avail != null && !avail.isBlank())
                ? Arrays.asList(avail.split(",")) : List.of();
            model.addAttribute("selectedSlots", selected);
        }
        return "member/availability";
    }

    @PostMapping("/availability")
    public String saveAvailability(Authentication auth,
            @RequestParam(value="slots", required=false) List<String> slots,
            RedirectAttributes ra) {

        User user = getCurrentUser(auth);
        if (user.getVolunteer() == null) {
            ra.addFlashAttribute("error", "Profil benevole non lie.");
            return "redirect:/member";
        }

        // Whitelist stricte
        List<String> ALLOWED = List.of("LUNDI_MATIN","LUNDI_SOIREE","MARDI_MATIN","MARDI_SOIREE",
            "MERCREDI_MATIN","MERCREDI_SOIREE","JEUDI_MATIN","JEUDI_SOIREE",
            "VENDREDI_MATIN","VENDREDI_SOIREE","SAMEDI","DIMANCHE");

        String sanitized = slots == null ? "" : slots.stream()
            .filter(ALLOWED::contains).distinct()
            .reduce((a,b) -> a+","+b).orElse("");

        Volunteer vol = user.getVolunteer();
        vol.setAvailability(sanitized);
        volunteerRepo.save(vol);

        // Points pour profil mis a jour
        if (!sanitized.isBlank()) {
            pointsService.addPoints(auth.getName(), 5, "Mise a jour disponibilites");
        }

        logService.log("Disponibilites MAJ: " + auth.getName(),
            ActivityLog.ActionType.UPDATE, "Disponibilite", auth.getName(), sanitized);

        ra.addFlashAttribute("success", "Disponibilites mises a jour !");
        return "redirect:/member";
    }

    private int getNextBadgePoints(Integer points) {
        if (points == null || points < 50)  return 50;
        if (points < 150) return 150;
        if (points < 400) return 400;
        if (points < 1000) return 1000;
        return 9999;
    }
}
