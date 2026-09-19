package se.comerit.resurs.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_events",
        indexes = {
                @Index(
                        name = "idx_audit_application_id",
                        columnList = "application_id"
                ),
                @Index(
                        name = "idx_audit_action",
                        columnList = "action"
                )
        }
)
@Data
@NoArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    @Column(name = "ts", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "actor", length = 100)
    private String actor;

    @Column(name = "details", columnDefinition = "jsonb")
    private String details;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}