package com.example.megrine.config;

import com.example.megrine.model.User;
import com.example.megrine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private UserRepository userRepo;

    // Disponible dans TOUS les templates automatiquement
    @ModelAttribute("pendingCount")
    public long pendingCount() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() ||
                auth.getName().equals("anonymousUser")) return 0;

            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) return 0;

            return userRepo.countByAccountStatus(User.AccountStatus.PENDING);
        } catch (Exception e) {
            return 0;
        }
    }
}
