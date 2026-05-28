package com.example.megrine.controller;

import com.example.megrine.model.User;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.UserRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ActivityLogService logService;

    @GetMapping
    public String list(@RequestParam(required = false) String perm, Model model) {
        try {
            List<User> users = userRepo.findAll();
            // Filtrer par permission si demande
            if (perm != null && !perm.isBlank()) {
                users = users.stream()
                    .filter(u -> u.getRole().equals("ROLE_ADMIN") ||
                                 u.getPermissions() == null ||
                                 u.getPermissions().isBlank() ||
                                 u.getPermissions().contains(perm))
                    .collect(Collectors.toList());
                model.addAttribute("filterPerm", perm);
            }
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("users", java.util.Collections.emptyList());
            model.addAttribute("error", "Erreur lors du chargement des utilisateurs.");
        }
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new User());
        return "users/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User user = userRepo.findById(id).orElseThrow();
        model.addAttribute("user", user);
        List<String> perms = user.getPermissions() != null && !user.getPermissions().isBlank()
            ? List.of(user.getPermissions().split(","))
            : List.of();
        model.addAttribute("selectedPerms", perms);
        return "users/form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(value="id", required=false) Long id,
            @RequestParam("username") String username,
            @RequestParam(value="password", required=false) String password,
            @RequestParam(value="fullName", required=false) String fullName,
            @RequestParam(value="email", required=false) String email,
            @RequestParam(value="role", defaultValue="ROLE_USER") String role,
            @RequestParam(value="enabled", required=false) String enabled,
            @RequestParam(value="permissions", required=false) List<String> permissions,
            RedirectAttributes ra) {

        boolean isNew = (id == null);
        User user = isNew ? new User() : userRepo.findById(id).orElse(new User());
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled("true".equals(enabled) || "on".equals(enabled));

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        } else if (isNew) {
            ra.addFlashAttribute("error", "Le mot de passe est obligatoire.");
            return "redirect:/users/new";
        }

        if (role.equals("ROLE_ADMIN")) {
            user.setPermissions("");
        } else if (permissions != null && !permissions.isEmpty()) {
            String permsStr = permissions.stream()
                .filter(p -> List.of("VOLUNTEERS","FAMILIES","DONATIONS",
                                     "STOCK","EVENTS","TRAINING","MEMBER").contains(p))
                .distinct().collect(Collectors.joining(","));
            user.setPermissions(permsStr);
        } else {
            user.setPermissions("");
        }

        userRepo.save(user);
        String action = isNew ? "Creation" : "Modification";
        logService.log(action + " compte: " + username,
            isNew ? ActivityLog.ActionType.CREATE : ActivityLog.ActionType.UPDATE,
            "Utilisateur", username,
            "Role: " + role + " | Permissions: " + (user.getPermissions().isBlank() ? "Toutes" : user.getPermissions()));

        ra.addFlashAttribute("success", "Utilisateur " + (isNew ? "créé" : "modifié") + " !");
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            User u = userRepo.findById(id).orElseThrow();
            if ("admin".equals(u.getUsername())) {
                ra.addFlashAttribute("error", "Impossible de supprimer le compte admin principal.");
                return "redirect:/users";
            }
            userRepo.deleteById(id);
            logService.logDelete("Utilisateur", u.getUsername());
            ra.addFlashAttribute("success", "Utilisateur supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer.");
        }
        return "redirect:/users";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            User user = userRepo.findById(id).orElseThrow();
            user.setEnabled(!user.isEnabled());
            userRepo.save(user);
            logService.log((user.isEnabled()?"Activation":"Désactivation") + " compte: " + user.getUsername(),
                ActivityLog.ActionType.UPDATE, "Utilisateur", user.getUsername(), null);
            ra.addFlashAttribute("success", user.isEnabled() ? "Compte activé." : "Compte désactivé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur.");
        }
        return "redirect:/users";
    }

    @PostMapping("/permissions/{id}")
    public String updatePermissions(
            @PathVariable Long id,
            @RequestParam(value="permissions", required=false) List<String> permissions,
            RedirectAttributes ra) {
        try {
            User user = userRepo.findById(id).orElseThrow();
            if (user.getRole().equals("ROLE_ADMIN")) {
                ra.addFlashAttribute("error", "Les admins ont toujours un accès complet.");
                return "redirect:/users";
            }
            String permsStr = permissions != null
                ? permissions.stream()
                    .filter(p -> List.of("VOLUNTEERS","FAMILIES","DONATIONS",
                                         "STOCK","EVENTS","TRAINING","MEMBER").contains(p))
                    .distinct().collect(Collectors.joining(","))
                : "";
            user.setPermissions(permsStr);
            userRepo.save(user);
            ra.addFlashAttribute("success", "Permissions mises à jour pour " + user.getUsername());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur.");
        }
        return "redirect:/users";
    }
}
