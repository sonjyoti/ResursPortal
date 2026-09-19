package se.comerit.resurs.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import se.comerit.resurs.dto.ApplicationCreationResult;
import se.comerit.resurs.dto.ApplicationRequest;
import se.comerit.resurs.service.ApplicationService;

@Controller
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(
            ApplicationService applicationService) {

        this.applicationService =
                applicationService;
    }

    @PostMapping("/apply")
    public String submitApplication(
            @ModelAttribute ApplicationRequest request,
            HttpSession session) {

        ApplicationCreationResult result =
                applicationService.createApplication(
                        request);

        session.setAttribute(
                "companyId",
                result.getCompanyId());

        return "redirect:/application/"
                + result.getApplicationId();
    }
}