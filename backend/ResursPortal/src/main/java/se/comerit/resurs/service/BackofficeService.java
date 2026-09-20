package se.comerit.resurs.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.comerit.resurs.model.Application;
import se.comerit.resurs.model.AuditEvent;
import se.comerit.resurs.model.Document;
import se.comerit.resurs.repository.ApplicationRepository;
import se.comerit.resurs.repository.AuditEventRepository;
import se.comerit.resurs.repository.DocumentRepository;

import java.util.List;

@Service
public class BackofficeService {

    private final ApplicationRepository applicationRepository;
    private final AuditEventRepository auditEventRepository;
    private final DocumentRepository documentRepository;

    public BackofficeService(
            ApplicationRepository applicationRepository,
            AuditEventRepository auditEventRepository,
            DocumentRepository documentRepository) {

        this.applicationRepository = applicationRepository;
        this.auditEventRepository = auditEventRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Get applications waiting for manual review.
     */
    public Page<Application> getApplicationsUnderReview(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return applicationRepository.findByStatusOrderByCreatedAtAsc(
                "UNDER_REVIEW",
                pageable
        );
    }

    /**
     * Get applications that have already been decided.
     */
    public Page<Application> getDecidedApplications(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return applicationRepository.findByStatusInOrderByUpdatedAtDesc(
                List.of("APPROVED", "REJECTED"),
                pageable
        );
    }

    /**
     * Approve or reject an application.
     */
    @Transactional
    public void decideApplication(
            Long applicationId,
            String decision,
            String comment,
            String workerName) {

        if (!"APPROVED".equals(decision)
                && !"REJECTED".equals(decision)) {

            throw new IllegalArgumentException(
                    "Invalid decision: " + decision
            );
        }

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Application not found: "
                                                + applicationId
                                )
                        );

        /*
         * Prevent changing an application that has
         * already been decided.
         */
        if (!"UNDER_REVIEW".equals(application.getStatus())) {
            throw new IllegalStateException(
                    "Application is not under review"
            );
        }

        application.setStatus(decision);
        application.setDecision(decision);

        /*
         * Because this method is transactional and the entity
         * is managed by Hibernate, an explicit save() is not
         * strictly necessary here.
         */
        applicationRepository.save(application);

        /*
         * Store the decision as a proper audit event.
         */
        AuditEvent auditEvent = new AuditEvent();

        auditEvent.setApplication(application);
        auditEvent.setAction("MANUAL_DECISION");
        auditEvent.setActor(workerName);

        /*
         * If your AuditEvent has a details field,
         * store the comment there.
         */
        if (comment != null && !comment.isBlank()) {
            auditEvent.setDetails(comment);
        }

        auditEventRepository.save(auditEvent);

        /*
         * Email notification will be handled later
         * by NotificationService.
         */
    }

    /**
     * Get one application.
     */
    public Application getApplication(Long applicationId) {

        return applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Application not found: "
                                        + applicationId
                        )
                );
    }

    /**
     * Get documents belonging to an application.
     */
    public List<Document> getDocuments(Long applicationId) {

        return documentRepository
                .findByApplicationIdOrderByUploadedAtDesc(
                        applicationId
                );
    }

    /**
     * Get audit events belonging to an application.
     */
    public List<AuditEvent> getAuditEvents(Long applicationId) {

        return auditEventRepository
                .findByApplicationIdOrderByTimestampAsc(
                        applicationId
                );
    }
}