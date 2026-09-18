package se.comerit.resurs.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double requestedAmount;

    private String purpose;

    private Status status = Status.PENDING_DOCS;

    private String decision;

    private String decisionReason;

    private String scoring_result;

    private String audit_log;

    private LocalDateTime created_at;

    private LocalDateTime updated_at;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "application")
    private List<Document> documents;
}
