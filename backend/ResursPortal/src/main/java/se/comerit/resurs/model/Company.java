package se.comerit.resurs.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_org_number",
                        columnNames = "org_number")
        }
)
@Data
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_number", nullable = false, unique = true, length = 20)
    private String orgNumber;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "authorized_signatory", length = 100)
    private String authorizedSignatory;

    @OneToMany(mappedBy = "company")
    private List<Application> applications = new ArrayList<>();
}