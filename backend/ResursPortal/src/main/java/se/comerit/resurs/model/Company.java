package se.comerit.resurs.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orgNumber;

    private String companyName;

    private String authorizedSignatory;

    @OneToMany(mappedBy = "company")
    private List<Application> applications;
}
