package se.comerit.resurs.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.comerit.resurs.model.Application;
import se.comerit.resurs.model.AuditEvent;
import se.comerit.resurs.model.Document;
import se.comerit.resurs.service.BackofficeService;

import java.util.List;

@Controller
public class BackofficeController {

    private final BackofficeService backofficeService;

    public BackofficeController(
            BackofficeService backofficeService) {

        this.backofficeService = backofficeService;
    }

    @GetMapping("/backoffice")
    public String backofficeOverview(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page) {

        if (!isCaseWorker(session)) {
            return "redirect:/login";
        }

        int pageSize = 20;

        Page<Application> reviewApplications =
                backofficeService.getApplicationsUnderReview(
                        page,
                        pageSize
                );

        Page<Application> decidedApplications =
                backofficeService.getDecidedApplications(
                        0,
                        20
                );

        model.addAttribute(
                "reviewApplications",
                reviewApplications.getContent()
        );

        model.addAttribute(
                "decidedApplications",
                decidedApplications.getContent()
        );

        model.addAttribute(
                "reviewCount",
                reviewApplications.getTotalElements()
        );

        model.addAttribute(
                "reviewPage",
                reviewApplications
        );

        model.addAttribute(
                "workerName",
                session.getAttribute("workerName")
        );

        return "backoffice";
    }

    @PostMapping("/backoffice/decide")
    public String decide(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("decision") String decision,
            @RequestParam(
                    value = "comment",
                    defaultValue = ""
            ) String comment,
            HttpSession session) {

        if (!isCaseWorker(session)) {
            return "redirect:/login";
        }

        String workerName =
                (String) session.getAttribute("workerName");

        try {

            backofficeService.decideApplication(
                    applicationId,
                    decision,
                    comment,
                    workerName
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            return "redirect:/backoffice";
        }

        return "redirect:/backoffice";
    }

    @GetMapping("/backoffice/application/{id}")
    public String viewApplicationDetail(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!isCaseWorker(session)) {
            return "redirect:/login";
        }

        try {

            Application application =
                    backofficeService.getApplication(id);

            List<Document> documents =
                    backofficeService.getDocuments(id);

            List<AuditEvent> auditEvents =
                    backofficeService.getAuditEvents(id);

            model.addAttribute(
                    "application",
                    application
            );

            model.addAttribute(
                    "documents",
                    documents
            );

            model.addAttribute(
                    "auditEvents",
                    auditEvents
            );

            model.addAttribute(
                    "workerName",
                    session.getAttribute("workerName")
            );

            return "backoffice_detail";

        } catch (IllegalArgumentException e) {

            return "redirect:/backoffice";
        }
    }

    private boolean isCaseWorker(HttpSession session) {

        return session.getAttribute("userId") != null
                && "caseWorker".equals(
                session.getAttribute("role")
        );
    }
}