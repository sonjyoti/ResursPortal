package se.comerit.resurs.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicationRequest {

    private String orgNumber;
    private String companyName;
    private String authorizedSignatory;

    private BigDecimal egetKapital;
    private BigDecimal totaltKapital;
    private BigDecimal omsattningstillgangar;
    private BigDecimal kortfristigaSkulder;
    private BigDecimal totalaSkulder;
    private BigDecimal rorelseresultat;
    private BigDecimal nettoomsattning;

    private BigDecimal requestedAmount;
    private String purpose;

    private BigDecimal operativtKassaflode;
    private BigDecimal investeringsKassaflode;
    private BigDecimal ranteKostnader;

    private String bransch;
}
