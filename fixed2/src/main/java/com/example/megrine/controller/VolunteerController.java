package com.example.megrine.controller;

import com.example.megrine.model.Volunteer;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.VolunteerRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/volunteers")
public class VolunteerController {

    @Autowired private VolunteerRepository volunteerRepo;
    @Autowired private ActivityLogService logService;

    @GetMapping
    public String list(Model model, @RequestParam(required=false) String search) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("volunteers", volunteerRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search));
            model.addAttribute("search", search);
        } else { model.addAttribute("volunteers", volunteerRepo.findAll()); }
        return "volunteers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Volunteer v = new Volunteer(); v.setStatus(Volunteer.VolunteerStatus.ACTIVE); v.setActive(true);
        model.addAttribute("volunteer", v); return "volunteers/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value="id",required=false) Long id,
            @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
            @RequestParam(value="email",required=false) String email,
            @RequestParam(value="phone",required=false) String phone,
            @RequestParam(value="bloodType",required=false) String bloodType,
            @RequestParam(value="cin",required=false) String cin,
            @RequestParam(value="address",required=false) String address,
            @RequestParam(value="joinDate",required=false) String joinDate,
            @RequestParam(value="status",defaultValue="ACTIVE") String status,
            @RequestParam(value="active",required=false) String active,
            RedirectAttributes ra) {
        boolean isNew = (id == null);
        Volunteer vol = isNew ? new Volunteer() : volunteerRepo.findById(id).orElse(new Volunteer());
        vol.setFirstName(firstName); vol.setLastName(lastName); vol.setEmail(email);
        vol.setPhone(phone); vol.setBloodType((bloodType!=null&&!bloodType.isBlank())?bloodType:null);
        vol.setCin(cin);
        vol.setAddress(address); vol.setActive("true".equals(active)||"on".equals(active));
        try { vol.setStatus(Volunteer.VolunteerStatus.valueOf(status)); } catch (Exception e) { vol.setStatus(Volunteer.VolunteerStatus.ACTIVE); }
        if (joinDate != null && !joinDate.isBlank()) { try { vol.setJoinDate(LocalDate.parse(joinDate)); } catch (Exception ignored) {} }
        else if (vol.getJoinDate() == null) { vol.setJoinDate(LocalDate.now()); }
        volunteerRepo.save(vol);
        String fullName = firstName + " " + lastName;
        String details = "Tel: " + phone + " | Email: " + email + " | Groupe sang: " + bloodType + " | Statut: " + status;
        if (isNew) logService.log("Ajout benevole: " + fullName, ActivityLog.ActionType.CREATE, "Benevole", fullName, details);
        else logService.log("Modification benevole: " + fullName, ActivityLog.ActionType.UPDATE, "Benevole", fullName, details);
        ra.addFlashAttribute("success", "Benevole enregistre !");
        return "redirect:/volunteers";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) { model.addAttribute("volunteer", volunteerRepo.findById(id).orElseThrow()); return "volunteers/form"; }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Volunteer v = volunteerRepo.findById(id).orElseThrow();
            String name = v.getFullName();
            volunteerRepo.deleteById(id);
            logService.log("Suppression benevole: " + name, ActivityLog.ActionType.DELETE, "Benevole", name, "Supprime definitivement");
            ra.addFlashAttribute("success", "Benevole supprime.");
        } catch (Exception e) { ra.addFlashAttribute("error", "Impossible de supprimer."); }
        return "redirect:/volunteers";
    }
}
