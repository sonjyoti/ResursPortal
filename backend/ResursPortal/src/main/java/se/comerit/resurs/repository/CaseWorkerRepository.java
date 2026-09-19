package se.comerit.resurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.resurs.model.CaseWorker;

import java.util.Optional;

@Repository
public interface CaseWorkerRepository extends JpaRepository<CaseWorker, Long> {

    Optional<CaseWorker> findByEmail(String email);
}