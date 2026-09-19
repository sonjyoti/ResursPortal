package se.comerit.resurs.dto;

import lombok.Data;

@Data
public class ApplicationCreationResult {

    private final Long applicationId;
    private final Long companyId;

    public ApplicationCreationResult(
            Long applicationId,
            Long companyId) {

        this.applicationId = applicationId;
        this.companyId = companyId;
    }
}