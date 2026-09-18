package se.comerit.resurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.resurs.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
}
