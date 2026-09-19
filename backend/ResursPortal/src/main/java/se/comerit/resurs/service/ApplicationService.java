package se.comerit.resurs.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.comerit.resurs.dto.ApplicationCreationResult;
import se.comerit.resurs.dto.ApplicationRequest;
import se.comerit.resurs.dto.ScoringResult;
import se.comerit.resurs.model.Application;
import se.comerit.resurs.model.Company;
import se.comerit.resurs.repository.ApplicationRepository;
import se.comerit.resurs.repository.CompanyRepository;

@Service
public class ApplicationService {

    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final ScoringService scoringService;
    private final AuditService auditService;

    public ApplicationService(
            CompanyRepository companyRepository,
            ApplicationRepository applicationRepository,
            ScoringService scoringService,
            AuditService auditService) {

        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
        this.scoringService = scoringService;
        this.auditService = auditService;
    }

    @Transactional
    public ApplicationCreationResult createApplication(
            ApplicationRequest request) {

        Company company =
                companyRepository
                        .findByOrgNumber(request.getOrgNumber())
                        .orElseGet(() -> {

                            Company newCompany =
                                    new Company();

                            newCompany.setOrgNumber(
                                    request.getOrgNumber());

                            newCompany.setCompanyName(
                                    request.getCompanyName());

                            newCompany.setAuthorizedSignatory(
                                    request.getAuthorizedSignatory());

                            return companyRepository.save(
                                    newCompany);
                        });

        ScoringResult scoring =
                scoringService.calculate(request);

        Application application =
                new Application();

        application.setCompany(company);
        application.setRequestedAmount(
                request.getRequestedAmount());
        application.setPurpose(
                request.getPurpose());

        application.setStatus(
                scoring.getStatus());

        if (!"REVIEW".equals(scoring.getDecision())) {
            application.setDecision(
                    scoring.getDecision());
        }

        application.setDecisionReason(
                scoring.getDecisionReason());

        application.setScoringResult(
                scoring.getScoringLog());

        Application saved =
                applicationRepository.save(application);

        auditService.record(
                saved,
                "APPLICATION_CREATED",
                "SYSTEM"
        );

        auditService.record(
                saved,
                "SCORING_RUN",
                "SYSTEM"
        );

        return new ApplicationCreationResult(
                saved.getId(),
                company.getId()
        );
    }
}