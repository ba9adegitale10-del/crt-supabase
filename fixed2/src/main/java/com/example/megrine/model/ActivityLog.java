package com.example.megrine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_name")
    private String entityName;

    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ActionType actionType = ActionType.CREATE;

    public enum ActionType {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, VIEW
    }
}
