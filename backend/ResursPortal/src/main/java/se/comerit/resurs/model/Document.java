package se.comerit.resurs.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity

public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String docType;

    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}
