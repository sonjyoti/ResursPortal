package se.comerit.resurs.service;

import org.springframework.stereotype.Service;

import se.comerit.resurs.model.Application;
import se.comerit.resurs.model.AuditEvent;
import se.comerit.resurs.repository.AuditEventRepository;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(
            AuditEventRepository auditEventRepository) {

        this.auditEventRepository =
                auditEventRepository;
    }

    public void record(
            Application application,
            String action,
            String actor) {

        AuditEvent event =
                new AuditEvent();

        event.setApplication(application);
        event.setAction(action);
        event.setActor(actor);

        auditEventRepository.save(event);
    }
}