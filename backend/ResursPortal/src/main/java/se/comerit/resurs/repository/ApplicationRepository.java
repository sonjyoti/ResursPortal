package se.comerit.resurs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.comerit.resurs.model.Application;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCompanyIdOrderByCreatedAtDesc(
            Long companyId
    );

    List<Application> findByStatusOrderByCreatedAtDesc(
            String status
    );
    Page<Application> findByStatusOrderByCreatedAtAsc(
            String status,
            Pageable pageable
    );
    Page<Application> findByStatusInOrderByUpdatedAtDesc(
            List<String> statuses,
            Pageable pageable
    );
}