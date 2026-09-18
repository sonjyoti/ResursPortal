package se.comerit.resurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.resurs.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
}
