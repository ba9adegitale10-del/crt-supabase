package com.example.megrine.controller;

import com.example.megrine.model.Donation;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.DonationRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/donations")
public class DonationController {

    @Autowired private DonationRepository donationRepo;
    @Autowired private ActivityLogService logService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("donations", donationRepo.findAll());
        model.addAttribute("totalAmount", donationRepo.sumMonetaryDonations());
        return "donations/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Donation d = new Donation(); d.setType(Donation.DonationType.MONETARY); d.setStatus(Donation.DonationStatus.RECEIVED);
        model.addAttribute("donation", d); return "donations/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value="id",required=false) Long id,
            @RequestParam(value="donorName",required=false) String donorName,
            @RequestParam(value="donorEmail",required=false) String donorEmail,
            @RequestParam(value="donorPhone",required=false) String donorPhone,
            @RequestParam(value="amount",required=false) String amount,
            @RequestParam(value="type",defaultValue="MONETARY") String type,
            @RequestParam(value="status",defaultValue="RECEIVED") String status,
            @RequestParam(value="donationDate",required=false) String donationDate,
            @RequestParam(value="description",required=false) String description,
            RedirectAttributes ra) {
        boolean isNew = (id == null);
        Donation donation = isNew ? new Donation() : donationRepo.findById(id).orElse(new Donation());
        donation.setDonorName(donorName); donation.setDonorEmail(donorEmail);
        donation.setDonorPhone(donorPhone); donation.setDescription(description);
        try { donation.setAmount(new BigDecimal(amount)); } catch (Exception e) { donation.setAmount(BigDecimal.ZERO); }
        try { donation.setType(Donation.DonationType.valueOf(type)); } catch (Exception e) { donation.setType(Donation.DonationType.MONETARY); }
        try { donation.setStatus(Donation.DonationStatus.valueOf(status)); } catch (Exception e) { donation.setStatus(Donation.DonationStatus.RECEIVED); }
        if (donationDate != null && !donationDate.isBlank()) { try { donation.setDonationDate(LocalDate.parse(donationDate)); } catch (Exception ignored) {} }
        else if (donation.getDonationDate() == null) { donation.setDonationDate(LocalDate.now()); }
        donationRepo.save(donation);
        String donor = donorName != null ? donorName : "Anonyme";
        String details = "Type: " + type + " | Montant: " + amount + " TND | Statut: " + status + " | " + description;
        if (isNew) logService.log("Nouveau don de: " + donor, ActivityLog.ActionType.CREATE, "Don", donor, details);
        else logService.log("Modification don de: " + donor, ActivityLog.ActionType.UPDATE, "Don", donor, details);
        ra.addFlashAttribute("success", "Don enregistre !");
        return "redirect:/donations";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) { model.addAttribute("donation", donationRepo.findById(id).orElseThrow()); return "donations/form"; }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Donation d = donationRepo.findById(id).orElseThrow();
            String donor = d.getDonorName() != null ? d.getDonorName() : "Anonyme";
            donationRepo.deleteById(id);
            logService.log("Suppression don de: " + donor, ActivityLog.ActionType.DELETE, "Don", donor, "Montant: " + d.getAmount() + " TND");
            ra.addFlashAttribute("success", "Don supprime.");
        } catch (Exception e) { ra.addFlashAttribute("error", "Impossible de supprimer."); }
        return "redirect:/donations";
    }
}
