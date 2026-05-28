package com.example.megrine.controller;

import com.example.megrine.model.Event;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.EventRepository;
import com.example.megrine.repository.FamilyRepository;
import com.example.megrine.repository.VolunteerRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired private EventRepository eventRepo;
    @Autowired private FamilyRepository familyRepo;
    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private ActivityLogService logService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("events", eventRepo.findAllByOrderByEventDateDesc());
        model.addAttribute("upcomingCount", eventRepo.countByStatus(Event.EventStatus.UPCOMING));
        model.addAttribute("completedCount", eventRepo.countByStatus(Event.EventStatus.COMPLETED));
        return "events/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("volunteers", volunteerRepo.findAll());
        model.addAttribute("families", familyRepo.findAll());
        return "events/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value="id",required=false) Long id,
            @RequestParam("title") String title,
            @RequestParam(value="description",required=false) String description,
            @RequestParam(value="location",required=false) String location,
            @RequestParam(value="eventDate",required=false) String eventDate,
            @RequestParam(value="responsibleName",required=false) String responsibleName,
            @RequestParam(value="volunteerNames",required=false) String volunteerNames,
            @RequestParam(value="familyNames",required=false) String familyNames,
            @RequestParam(value="participantsCount",required=false) Integer participantsCount,
            @RequestParam(value="notes",required=false) String notes,
            @RequestParam(value="type",defaultValue="HUMANITARIAN") String type,
            @RequestParam(value="status",defaultValue="UPCOMING") String status,
            RedirectAttributes ra) {
        boolean isNew = (id == null);
        Event event = isNew ? new Event() : eventRepo.findById(id).orElse(new Event());
        event.setTitle(title); event.setDescription(description); event.setLocation(location);
        event.setResponsibleName(responsibleName); event.setVolunteerNames(volunteerNames);
        event.setFamilyNames(familyNames); event.setParticipantsCount(participantsCount); event.setNotes(notes);
        try { event.setType(Event.EventType.valueOf(type)); } catch (Exception e) { event.setType(Event.EventType.HUMANITARIAN); }
        try { event.setStatus(Event.EventStatus.valueOf(status)); } catch (Exception e) { event.setStatus(Event.EventStatus.UPCOMING); }
        if (eventDate != null && !eventDate.isBlank()) { try { event.setEventDate(LocalDate.parse(eventDate)); } catch (Exception ignored) {} }
        eventRepo.save(event);
        String details = "Lieu: " + location + " | Date: " + eventDate + " | Resp: " + responsibleName + " | Participants: " + participantsCount + " | Statut: " + status;
        if (isNew) logService.log("Nouvel evenement: " + title, ActivityLog.ActionType.CREATE, "Evenement", title, details);
        else logService.log("Modification evenement: " + title, ActivityLog.ActionType.UPDATE, "Evenement", title, details);
        ra.addFlashAttribute("success", "Evenement enregistre !");
        return "redirect:/events";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventRepo.findById(id).orElseThrow());
        model.addAttribute("volunteers", volunteerRepo.findAll());
        model.addAttribute("families", familyRepo.findAll());
        return "events/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Event event = eventRepo.findById(id).orElseThrow();
            String title = event.getTitle();
            eventRepo.deleteById(id);
            logService.log("Suppression evenement: " + title, ActivityLog.ActionType.DELETE, "Evenement", title, "Date: " + event.getEventDate());
            ra.addFlashAttribute("success", "Evenement supprime.");
        } catch (Exception e) { ra.addFlashAttribute("error", "Impossible de supprimer."); }
        return "redirect:/events";
    }
}
