package se.comerit.resurs.service;

import org.springframework.stereotype.Service;
import se.comerit.resurs.dto.ApplicationRequest;
import se.comerit.resurs.dto.ScoringResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScoringService {

    public ScoringResult calculate(ApplicationRequest request) {

        double egetKapital = value(request.getEgetKapital());
        double totaltKapital = value(request.getTotaltKapital());
        double omsattningstillgangar = value(request.getOmsattningstillgangar());
        double kortfristigaSkulder = value(request.getKortfristigaSkulder());
        double totalaSkulder = value(request.getTotalaSkulder());
        double rorelseresultat = value(request.getRorelseresultat());
        double nettoomsattning = value(request.getNettoomsattning());

        BigDecimal requestedAmount =
                request.getRequestedAmount() != null
                        ? request.getRequestedAmount()
                        : BigDecimal.ZERO;

        double operativtKassaflode =
                value(request.getOperativtKassaflode());

        double investeringsKassaflode =
                value(request.getInvesteringsKassaflode());

        double ranteKostnader =
                value(request.getRanteKostnader());

        String bransch =
                request.getBransch() == null
                        ? ""
                        : request.getBransch();

        StringBuilder scoringLog = new StringBuilder();
        StringBuilder decisionReason = new StringBuilder();

        int flagCount = 0;
        boolean hardReject = false;

        int kreditPoang = 100;

        // ==========================================
        // SOLIDITET
        // ==========================================

        double soliditet = 0.0;

        if (totaltKapital != 0) {
            soliditet = egetKapital / totaltKapital;
        }

        scoringLog
                .append("soliditet=")
                .append(format(soliditet));

        if (soliditet < 0.20) {

            hardReject = true;

            decisionReason
                    .append("AVSLAG: Soliditet för låg (")
                    .append(format(soliditet))
                    .append(" < 0.20 gräns). ");

            scoringLog.append(" [REJECT]");
            kreditPoang -= 40;

        } else if (soliditet < 0.25) {

            flagCount++;

            decisionReason
                    .append("VARNING: Soliditet låg (")
                    .append(format(soliditet))
                    .append(", rekommenderad miniminivå 0.25). ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 20;

        } else {

            decisionReason
                    .append("Soliditet OK (")
                    .append(format(soliditet))
                    .append("). ");

            scoringLog.append(" [OK]");
            kreditPoang += 5;
        }

        scoringLog.append(", ");

        // ==========================================
        // LIKVIDITET
        // ==========================================

        double likviditetsgrad = 0.0;

        if (kortfristigaSkulder != 0) {
            likviditetsgrad =
                    omsattningstillgangar / kortfristigaSkulder;
        }

        scoringLog
                .append("likviditetsgrad=")
                .append(format(likviditetsgrad));

        if (likviditetsgrad < 1.0) {

            flagCount++;

            decisionReason
                    .append("VARNING: Likviditetsgrad under 1.0 (")
                    .append(format(likviditetsgrad))
                    .append("). Kortfristiga skulder överstiger omsättningstillgångar. ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 15;

        } else if (likviditetsgrad >= 2.0) {

            decisionReason
                    .append("Likviditetsgrad god (")
                    .append(format(likviditetsgrad))
                    .append("). ");

            scoringLog.append(" [GOOD]");
            kreditPoang += 10;

        } else {

            decisionReason
                    .append("Likviditetsgrad godkänd (")
                    .append(format(likviditetsgrad))
                    .append("). ");

            scoringLog.append(" [OK]");
        }

        scoringLog.append(", ");

        // ==========================================
        // SKULDSÄTTNINGSGRAD
        // ==========================================

        double skuldsattningsgrad = 0.0;

        if (egetKapital != 0) {
            skuldsattningsgrad =
                    totalaSkulder / egetKapital;
        }

        scoringLog
                .append("skuldsättningsgrad=")
                .append(format(skuldsattningsgrad));

        if (skuldsattningsgrad > 3.0) {

            hardReject = true;

            decisionReason
                    .append("AVSLAG: Skuldsättningsgrad för hög (")
                    .append(format(skuldsattningsgrad))
                    .append(" > 3.0). ");

            scoringLog.append(" [REJECT]");
            kreditPoang -= 35;

        } else if (skuldsattningsgrad > 2.0) {

            flagCount++;

            decisionReason
                    .append("VARNING: Skuldsättningsgrad hög (")
                    .append(format(skuldsattningsgrad))
                    .append(", rekommenderas under 2.0). ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 15;

        } else {

            decisionReason
                    .append("Skuldsättningsgrad OK (")
                    .append(format(skuldsattningsgrad))
                    .append("). ");

            scoringLog.append(" [OK]");
            kreditPoang += 5;
        }

        scoringLog.append(", ");

        // ==========================================
        // RÖRELSEMARGINAL
        // ==========================================

        double rorelsemarginal = 0.0;

        if (nettoomsattning != 0) {
            rorelsemarginal =
                    rorelseresultat / nettoomsattning;
        }

        scoringLog
                .append("rörelsemarginal=")
                .append(format(rorelsemarginal));

        if (rorelsemarginal < 0.02) {

            flagCount++;

            decisionReason
                    .append("VARNING: Rörelseresultatmarginal låg (")
                    .append(format(rorelsemarginal * 100))
                    .append("%, rekommenderas över 2%). ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 10;

        } else if (rorelsemarginal >= 0.10) {

            decisionReason
                    .append("Rörelseresultatmarginal god (")
                    .append(format(rorelsemarginal * 100))
                    .append("%). ");

            scoringLog.append(" [GOOD]");
            kreditPoang += 8;

        } else {

            decisionReason
                    .append("Rörelseresultatmarginal godkänd (")
                    .append(format(rorelsemarginal * 100))
                    .append("%). ");

            scoringLog.append(" [OK]");
        }

        // ==========================================
        // EXTRA SOLIDITET CHECK
        // ==========================================

        if (soliditet < 0.30 &&
                requestedAmount.compareTo(
                        new BigDecimal("1000000")) > 0) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Stor kreditbelopp med soliditet under 0.30 – extra granskning rekommenderas. "
            );

            scoringLog.append(
                    ", storkredit_soliditet [FLAGGED]"
            );

            kreditPoang -= 12;
        }

        // ==========================================
        // LIKVIDITET MARGINAL
        // ==========================================

        if (likviditetsgrad < 1.2 &&
                likviditetsgrad >= 1.0) {

            flagCount++;

            decisionReason
                    .append("VARNING: Likviditetsgrad nära minimigräns (")
                    .append(format(likviditetsgrad))
                    .append(" < 1.2). ");

            scoringLog.append(
                    ", likviditet_marginal [FLAGGED]"
            );

            kreditPoang -= 8;
        }

        // ==========================================
        // LARGE CREDIT
        // ==========================================

        if (requestedAmount.compareTo(
                new BigDecimal("5000000")) > 0) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Kreditbelopp överstiger 5 000 000 kr — kräver manuell granskning. "
            );

            scoringLog.append(
                    ", storkredit [FLAGGED]"
            );

            kreditPoang -= 10;
        }

        // ==========================================
        // NEGATIVE EQUITY
        // ==========================================

        if (egetKapital < 0) {

            hardReject = true;

            decisionReason.append(
                    "AVSLAG: Negativt eget kapital. "
            );

            scoringLog.append(
                    ", negativt_eget_kapital [REJECT]"
            );

            kreditPoang -= 50;
        }

        // ==========================================
        // LOW REVENUE
        // ==========================================

        if (nettoomsattning < 500000) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Låg nettoomsättning (under 500 000 kr). "
            );

            scoringLog.append(
                    ", låg_omsättning [FLAGGED]"
            );

            kreditPoang -= 7;
        }

        // ==========================================
        // NEGATIVE OPERATING RESULT
        // ==========================================

        if (rorelseresultat < 0) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Negativt rörelseresultat. "
            );

            scoringLog.append(
                    ", negativt_rörelseresultat [FLAGGED]"
            );

            kreditPoang -= 12;
        }

        // ==========================================
        // DEBT VS REVENUE
        // ==========================================

        if (totalaSkulder > nettoomsattning * 2) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Totala skulder överstiger dubbla nettoomsättningen. "
            );

            scoringLog.append(
                    ", skulder_vs_omsattning [FLAGGED]"
            );

            kreditPoang -= 10;
        }

        // ==========================================
        // INDUSTRY FACTOR
        // ==========================================

        double branschFaktor = getBranschFaktor(bransch);

        scoringLog
                .append(", bransch=")
                .append(bransch.isEmpty()
                        ? "OKÄND"
                        : bransch)
                .append("(faktor=")
                .append(format(branschFaktor))
                .append(")");

        double branschJusteradSoliditetGrans =
                0.20 * branschFaktor;

        if (soliditet < branschJusteradSoliditetGrans) {

            flagCount++;

            decisionReason
                    .append("VARNING: Soliditet understiger branschjusterad gräns (")
                    .append(format(branschJusteradSoliditetGrans))
                    .append(" för bransch ")
                    .append(bransch)
                    .append("). ");

            scoringLog.append(
                    ", bransch_soliditet [FLAGGED]"
            );

            kreditPoang -= 8;
        }

        // ==========================================
        // HISTORICAL INDUSTRY COMPARISON
        // ==========================================

        Map<String, Double> branschSnittSoliditet =
                new HashMap<>();

        branschSnittSoliditet.put("BYGG", 0.22);
        branschSnittSoliditet.put("HANDEL", 0.28);
        branschSnittSoliditet.put("IT", 0.45);
        branschSnittSoliditet.put("FASTIGHET", 0.18);
        branschSnittSoliditet.put("TILLVERKNING", 0.30);
        branschSnittSoliditet.put("TRANSPORT", 0.20);
        branschSnittSoliditet.put("RESTAURANG", 0.15);
        branschSnittSoliditet.put("FINANS", 0.35);
        branschSnittSoliditet.put("VÅRD", 0.38);
        branschSnittSoliditet.put("UTBILDNING", 0.32);

        Map<String, Double> branschSnittMarginal =
                new HashMap<>();

        branschSnittMarginal.put("BYGG", 0.04);
        branschSnittMarginal.put("HANDEL", 0.03);
        branschSnittMarginal.put("IT", 0.15);
        branschSnittMarginal.put("FASTIGHET", 0.12);
        branschSnittMarginal.put("TILLVERKNING", 0.06);
        branschSnittMarginal.put("TRANSPORT", 0.03);
        branschSnittMarginal.put("RESTAURANG", 0.05);
        branschSnittMarginal.put("FINANS", 0.18);
        branschSnittMarginal.put("VÅRD", 0.07);
        branschSnittMarginal.put("UTBILDNING", 0.08);

        if (branschSnittSoliditet.containsKey(bransch)) {

            double snittSoliditet =
                    branschSnittSoliditet.get(bransch);

            if (soliditet < snittSoliditet * 0.75) {

                flagCount++;

                decisionReason
                        .append("VARNING: Soliditet betydligt under branschsnitt för ")
                        .append(bransch)
                        .append(" (snitt=")
                        .append(format(snittSoliditet))
                        .append("). ");

                scoringLog.append(
                        ", under_branschsnitt_soliditet [FLAGGED]"
                );

                kreditPoang -= 6;
            }
        }

        if (branschSnittMarginal.containsKey(bransch)) {

            double snittMarginal =
                    branschSnittMarginal.get(bransch);

            if (rorelsemarginal < snittMarginal * 0.5) {

                flagCount++;

                decisionReason
                        .append("VARNING: Rörelsemarginal under 50% av branschsnitt för ")
                        .append(bransch)
                        .append(". ");

                scoringLog.append(
                        ", under_branschsnitt_marginal [FLAGGED]"
                );

                kreditPoang -= 5;
            }
        }

        // ==========================================
        // CASH FLOW
        // ==========================================

        double kassaflodeKvot = 0.0;

        if (totalaSkulder != 0) {
            kassaflodeKvot =
                    operativtKassaflode / totalaSkulder;
        }

        scoringLog
                .append(", kassaflödeskvot=")
                .append(String.format("%.3f", kassaflodeKvot));

        if (kassaflodeKvot < 0) {

            hardReject = true;

            decisionReason
                    .append("AVSLAG: Negativt operativt kassaflöde (kassaflödeskvot=")
                    .append(String.format("%.3f", kassaflodeKvot))
                    .append("). ");

            scoringLog.append(" [REJECT]");
            kreditPoang -= 30;

        } else if (kassaflodeKvot < 0.05) {

            flagCount++;

            decisionReason
                    .append("VARNING: Kassaflödeskvot låg (")
                    .append(String.format("%.3f", kassaflodeKvot))
                    .append(" < 0.05). ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 12;

        } else if (kassaflodeKvot < 0.08) {

            flagCount++;

            decisionReason
                    .append("VARNING: Kassaflödeskvot under rekommenderad nivå (")
                    .append(String.format("%.3f", kassaflodeKvot))
                    .append(" < 0.08). ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 6;

        } else {

            decisionReason.append(
                    "Kassaflödeskvot OK ("
            ).append(
                    String.format("%.3f", kassaflodeKvot)
            ).append("). ");

            scoringLog.append(" [OK]");
            kreditPoang += 5;
        }

        // ==========================================
        // INVESTMENT CASH FLOW
        // ==========================================

        if (investeringsKassaflode <
                -nettoomsattning * 0.3) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Högt negativt investeringskassaflöde ("
            ).append(
                    String.format("%.0f",
                            investeringsKassaflode)
            ).append(" kr). ");

            scoringLog.append(
                    ", inv_kassaflode [FLAGGED]"
            );

            kreditPoang -= 4;
        }

        // ==========================================
        // INTEREST COVERAGE
        // ==========================================

        double ranteTackningsgrad;

        if (ranteKostnader < 0 ||
                ranteKostnader == 0) {

            ranteTackningsgrad = 999;

        } else {

            ranteTackningsgrad =
                    rorelseresultat / ranteKostnader;
        }

        scoringLog
                .append(", ränteTäckning=")
                .append(format(ranteTackningsgrad));

        if (ranteTackningsgrad < 1.5) {

            hardReject = true;

            decisionReason
                    .append("AVSLAG: Räntetäckningsgrad under 1.5 (")
                    .append(format(ranteTackningsgrad))
                    .append("). Rörelseresultat täcker ej räntekostnader. ");

            scoringLog.append(" [REJECT]");
            kreditPoang -= 35;

        } else if (ranteTackningsgrad < 2.5) {

            flagCount++;

            decisionReason
                    .append("VARNING: Räntetäckningsgrad låg (")
                    .append(format(ranteTackningsgrad))
                    .append(" < 2.5, rekommenderas minst 2.5). ");

            scoringLog.append(" [FLAGGED]");
            kreditPoang -= 15;

        } else if (ranteTackningsgrad >= 999) {

            decisionReason.append(
                    "Räntetäckningsgrad ej tillämplig (inga räntekostnader). "
            );

            scoringLog.append(" [N/A]");

        } else {

            decisionReason.append(
                    "Räntetäckningsgrad OK ("
            ).append(
                    format(ranteTackningsgrad)
            ).append("). ");

            scoringLog.append(" [OK]");
            kreditPoang += 8;
        }

        // ==========================================
        // COMBINATION RULES
        // ==========================================

        if (soliditet < 0.25 &&
                skuldsattningsgrad > 2.5) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Dubbel riskindikator — låg soliditet kombinerat med hög skuldsättning. "
            );

            scoringLog.append(
                    ", kombinationsrisk_soliditet_skuld [FLAGGED]"
            );

            kreditPoang -= 18;
        }

        if (likviditetsgrad < 1.0 &&
                rorelseresultat < 0) {

            hardReject = true;

            decisionReason.append(
                    "AVSLAG: Kombinationsrisk — likviditetsgrad under 1.0 samt negativt rörelseresultat. "
            );

            scoringLog.append(
                    ", kombinationsrisk_likviditet_resultat [REJECT]"
            );

            kreditPoang -= 40;
        }

        if (requestedAmount.doubleValue() >
                nettoomsattning) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Kreditbelopp överstiger årsoms. "
            );

            scoringLog.append(
                    ", kredit_vs_omsattning [FLAGGED]"
            );

            kreditPoang -= 8;
        }

        if (requestedAmount.doubleValue() > 0 &&
                egetKapital /
                        requestedAmount.doubleValue() < 0.3) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Eget kapital täcker mindre än 30% av kreditbeloppet. "
            );

            scoringLog.append(
                    ", eget_kapital_vs_kredit [FLAGGED]"
            );

            kreditPoang -= 10;
        }

        double skuldTackningsFel =
                (totalaSkulder + kortfristigaSkulder) /
                        (nettoomsattning + 1);

        if (skuldTackningsFel > 2.0) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Skuldbörda hög relativt omsättning (kombinationscheck). "
            );

            scoringLog.append(
                    ", skuld_omsattning_kombination [FLAGGED]"
            );

            kreditPoang -= 7;
        }

        if (kassaflodeKvot < 0.05 &&
                skuldsattningsgrad > 2.0) {

            flagCount++;

            decisionReason.append(
                    "VARNING: Kombinationsrisk kassaflöde + skuldsättning. "
            );

            scoringLog.append(
                    ", kassaflode_skuld_kombination [FLAGGED]"
            );

            kreditPoang -= 12;
        }

        scoringLog
                .append(", kreditPoäng=")
                .append(kreditPoang);

        // ==========================================
        // FINAL DECISION
        // ==========================================

        String decision;
        String status;

        if (hardReject) {

            decision = "REJECTED";
            status = "REJECTED";

            decisionReason.insert(
                    0,
                    "=== ANSÖKAN AVSLAGEN === "
            );

        } else if (flagCount >= 1) {

            decision = "REVIEW";
            status = "UNDER_REVIEW";

            decisionReason.insert(
                    0,
                    "=== MANUELL GRANSKNING === Antal varningsflaggor: "
                            + flagCount + ". "
            );

        } else {

            decision = "APPROVED";
            status = "APPROVED";

            decisionReason.insert(
                    0,
                    "=== ANSÖKAN GODKÄND === Alla nyckeltal uppfyller krav. "
            );
        }

        return new ScoringResult(
                decision,
                status,
                decisionReason.toString(),
                scoringLog.toString(),
                flagCount,
                kreditPoang
        );
    }

    private double value(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    private double getBranschFaktor(String bransch) {

        return switch (bransch) {
            case "BYGG" -> 0.85;
            case "HANDEL" -> 1.10;
            case "IT" -> 1.20;
            case "FASTIGHET" -> 0.90;
            case "TILLVERKNING" -> 0.95;
            case "TRANSPORT" -> 0.88;
            case "RESTAURANG" -> 0.80;
            case "FINANS" -> 1.15;
            case "VÅRD" -> 1.05;
            case "UTBILDNING" -> 1.00;
            default -> 1.00;
        };
    }
}