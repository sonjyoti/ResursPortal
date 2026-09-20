package se.comerit.resurs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackofficeApplicationDetailDto {

    private Long id;
    private BigDecimal requestedAmount;
    private String purpose;
    private String status;
    private String decision;
    private String decisionReason;
    private String scoringResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String companyName;
    private String orgNumber;
    private String authorizedSignatory;
}