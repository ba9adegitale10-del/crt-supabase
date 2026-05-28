package com.example.megrine.config;

import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.ActivityLogRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ActivityLogRepository logRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {

        // Logger la connexion avec IP + role
        try {
            String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("INCONNU");

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();

            ActivityLog log = new ActivityLog();
            log.setUsername(auth.getName());
            log.setAction("Connexion au systeme");
            log.setActionType(ActivityLog.ActionType.LOGIN);
            log.setEntityType("Connexion");
            log.setEntityName(auth.getName());
            log.setDetails("Role: " + role + " | IP: " + ip + " | Navigateur: " + getBrowser(request));
            log.setIpAddress(ip);
            log.setCreatedAt(LocalDateTime.now());
            logRepo.save(log);
        } catch (Exception e) {
            // Silent fail
        }

        // Redirect based on role
        boolean isMember = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_MEMBER"));
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        if (isMember && !isAdmin && !isUser) {
            response.sendRedirect("/member");
        } else {
            response.sendRedirect("/dashboard");
        }
    }

    private String getBrowser(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Inconnu";
        if (ua.contains("Mobile")) return "Mobile";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari")) return "Safari";
        return "Navigateur";
    }
}
