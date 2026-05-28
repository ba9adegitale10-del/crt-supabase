package com.example.megrine.config;

import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.ActivityLogRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

    @Autowired
    private ActivityLogRepository logRepo;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {
        try {
            if (auth != null) {
                ActivityLog log = new ActivityLog();
                log.setUsername(auth.getName());
                log.setAction("Deconnexion du systeme");
                log.setActionType(ActivityLog.ActionType.LOGOUT);
                log.setEntityType("Connexion");
                log.setEntityName(auth.getName());
                log.setDetails("Session terminee normalement");
                log.setCreatedAt(LocalDateTime.now());
                logRepo.save(log);
            }
        } catch (Exception e) { /* silent */ }
        response.sendRedirect("/login?logout=true");
    }
}
