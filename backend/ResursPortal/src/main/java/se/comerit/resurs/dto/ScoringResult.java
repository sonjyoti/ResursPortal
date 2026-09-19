package se.comerit.resurs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoringResult {

    private String decision;
    private String status;
    private String decisionReason;
    private String scoringLog;
    private int flagCount;
    private int creditScore;
}