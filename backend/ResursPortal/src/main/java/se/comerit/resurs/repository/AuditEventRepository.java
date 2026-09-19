package se.comerit.resurs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.resurs.model.AuditEvent;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByApplicationIdOrderByTimestampAsc(
            Long applicationId
    );

    List<AuditEvent> findByActionOrderByTimestampDesc(
            String action
    );
}