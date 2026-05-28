package com.example.megrine.controller;

import com.example.megrine.model.*;
import com.example.megrine.repository.TrainingResourceRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

@Controller
@RequestMapping("/training")
public class TrainingController {

    @Autowired private TrainingResourceRepository trainingRepo;
    @Autowired private ActivityLogService logService;

    // SECURITE: accessible a tous les connectes (voir SecurityConfig)
    @GetMapping
    public String list(Model model, Authentication auth) {
        model.addAttribute("resources", trainingRepo.findByVisibleTrueOrderByUploadedAtDesc());
        model.addAttribute("categories", TrainingResource.ResourceCategory.values());
        logService.log("Consultation formations", ActivityLog.ActionType.VIEW,
            "Formation", auth.getName(), null);
        return "training/list";
    }

    // SECURITE: ADMIN seulement (voir SecurityConfig)
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("resource", new TrainingResource());
        model.addAttribute("categories", TrainingResource.ResourceCategory.values());
        return "training/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value="id",required=false) Long id,
            @RequestParam("title") String title,
            @RequestParam(value="description",required=false) String description,
            @RequestParam(value="url",required=false) String url,
            @RequestParam(value="fileType",defaultValue="LINK") String fileType,
            @RequestParam(value="category",defaultValue="GENERAL") String category,
            Authentication auth, RedirectAttributes ra) {

        // SECURITE: validation et nettoyage de l'URL
        if (url != null && !url.isBlank()) {
            // Rejeter URLs dangereuses
            String urlLower = url.toLowerCase().trim();
            if (!urlLower.startsWith("http://") && !urlLower.startsWith("https://")
                && !urlLower.startsWith("/")) {
                ra.addFlashAttribute("error", "URL invalide. Utilisez http://, https:// ou un chemin relatif.");
                return "redirect:/training/new";
            }
            // Taille max 500 chars
            if (url.length() > 500) {
                ra.addFlashAttribute("error", "URL trop longue (max 500 caracteres).");
                return "redirect:/training/new";
            }
        }

        TrainingResource res = (id != null) ? trainingRepo.findById(id).orElse(new TrainingResource()) : new TrainingResource();
        res.setTitle(title.trim().substring(0, Math.min(title.trim().length(), 200)));
        res.setDescription(description);
        res.setUrl(url);
        res.setFileType(fileType);
        res.setUploadedBy(auth.getName());
        res.setUploadedAt(LocalDate.now());
        res.setVisible(true);
        try { res.setCategory(TrainingResource.ResourceCategory.valueOf(category)); }
        catch (Exception e) { res.setCategory(TrainingResource.ResourceCategory.GENERAL); }
        trainingRepo.save(res);

        logService.log("Ajout formation: " + title, ActivityLog.ActionType.CREATE,
            "Formation", title, "Type: " + fileType + " | URL: " + url);
        ra.addFlashAttribute("success", "Formation ajoutee !");
        return "redirect:/training";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            TrainingResource res = trainingRepo.findById(id).orElseThrow();
            trainingRepo.deleteById(id);
            logService.log("Suppression formation: " + res.getTitle(),
                ActivityLog.ActionType.DELETE, "Formation", res.getTitle(), null);
            ra.addFlashAttribute("success", "Formation supprimee.");
        } catch (Exception e) { ra.addFlashAttribute("error", "Erreur."); }
        return "redirect:/training";
    }
}
