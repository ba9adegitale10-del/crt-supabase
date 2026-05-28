package com.example.megrine.controller;

import com.example.megrine.model.*;
import com.example.megrine.repository.*;
import com.example.megrine.service.ActivityLogService;
import com.example.megrine.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
public class ParticipationController {

    @Autowired private EventRepository eventRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private EventParticipationRepository participationRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ActivityLogService logService;
    @Autowired private PointsService pointsService;

    @PostMapping("/events/participate/{eventId}")
    public String participate(@PathVariable Long eventId,
            Authentication auth, RedirectAttributes ra) {
        try {
            User user = userRepo.findByUsername(auth.getName()).orElseThrow();

            if (user.getVolunteer() == null) {
                ra.addFlashAttribute("error", "Votre compte n'est pas lie a un profil benevole. Contactez l'admin.");
                return "redirect:/member";
            }

            Volunteer vol = user.getVolunteer();
            Event event = eventRepo.findById(eventId).orElseThrow();

            if (participationRepo.existsByEventIdAndVolunteerId(eventId, vol.getId())) {
                ra.addFlashAttribute("error", "Vous etes deja inscrit a cet evenement !");
                return "redirect:/member";
            }

            if (event.getParticipantsCount() != null) {
                long enrolled = participationRepo.countByEventId(eventId);
                if (enrolled >= event.getParticipantsCount()) {
                    ra.addFlashAttribute("error", "Cet evenement est complet !");
                    return "redirect:/member";
                }
            }

            if (event.getStatus() == Event.EventStatus.COMPLETED ||
                event.getStatus() == Event.EventStatus.CANCELLED) {
                ra.addFlashAttribute("error", "Cet evenement est termine ou annule.");
                return "redirect:/member";
            }

            EventParticipation p = new EventParticipation();
            p.setEvent(event); p.setVolunteer(vol);
            p.setEnrolledAt(LocalDateTime.now());
            p.setStatus(EventParticipation.ParticipationStatus.ENROLLED);
            participationRepo.save(p);

            // Gagner des points pour l'inscription
            pointsService.addPoints(auth.getName(),
                PointsService.POINTS_INSCRIPTION_EVENT,
                "Inscription evenement: " + event.getTitle());

            logService.log("Inscription evenement: " + event.getTitle(),
                ActivityLog.ActionType.CREATE, "Participation", vol.getFullName(),
                "Evenement: " + event.getTitle() + " | +" + PointsService.POINTS_INSCRIPTION_EVENT + " points");

            ra.addFlashAttribute("success", "Inscription confirmee ! +" + PointsService.POINTS_INSCRIPTION_EVENT + " points gagnes !");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de l'inscription: " + e.getMessage());
        }
        return "redirect:/member";
    }

    @PostMapping("/events/cancel/{eventId}")
    public String cancel(@PathVariable Long eventId,
            Authentication auth, RedirectAttributes ra) {
        try {
            User user = userRepo.findByUsername(auth.getName()).orElseThrow();
            if (user.getVolunteer() == null) return "redirect:/member";

            Volunteer vol = user.getVolunteer();
            EventParticipation p = participationRepo
                .findByEventIdAndVolunteerId(eventId, vol.getId()).orElseThrow();

            if (!p.getVolunteer().getId().equals(vol.getId())) {
                ra.addFlashAttribute("error", "Action non autorisee.");
                return "redirect:/member";
            }

            p.setStatus(EventParticipation.ParticipationStatus.CANCELLED);
            participationRepo.save(p);

            // Perdre les points d'inscription
            pointsService.removePoints(auth.getName(),
                PointsService.POINTS_INSCRIPTION_EVENT,
                "Annulation evenement: " + p.getEvent().getTitle());

            ra.addFlashAttribute("success", "Inscription annulee.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de l'annulation.");
        }
        return "redirect:/member";
    }

    // ADMIN: valider participation + donner heures + points
    @PostMapping("/events/participants/{participationId}/complete")
    public String complete(@PathVariable Long participationId,
            @RequestParam(value="hours", defaultValue="0") Integer hours,
            RedirectAttributes ra) {
        try {
            EventParticipation p = participationRepo.findById(participationId).orElseThrow();
            p.setStatus(EventParticipation.ParticipationStatus.COMPLETED);
            p.setHoursContributed(hours);
            participationRepo.save(p);

            Volunteer vol = p.getVolunteer();
            Integer totalHours = participationRepo.sumHoursByVolunteerId(vol.getId());
            vol.setTotalHours(totalHours != null ? totalHours : 0);
            volunteerRepo.save(vol);

            // Points pour participation complete (proportionnel aux heures)
            int pts = PointsService.POINTS_PARTICIPATION_COMPLETE + (hours * 5);
            pointsService.addPoints(vol.getFullName(), pts,
                "Participation validee: " + p.getEvent().getTitle() + " (" + hours + "h)");

            logService.log("Participation validee: " + vol.getFullName(),
                ActivityLog.ActionType.UPDATE, "Participation", vol.getFullName(),
                hours + "h | +" + pts + " points | Badge: " + userRepo.findByUsername(vol.getFullName()).map(User::getBadgeLabel).orElse("—"));

            ra.addFlashAttribute("success", hours + "h et " + pts + " points ajoutes a " + vol.getFullName());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur validation.");
        }
        return "redirect:/admin/dashboard";
    }
}
