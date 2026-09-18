package se.comerit.resurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.resurs.model.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application,Long> {
}
