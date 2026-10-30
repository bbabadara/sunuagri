package sn.sunagri.commercialisation.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.commercialisation.domain.PaiementDu;
import sn.sunagri.commercialisation.domain.StatutPaiement;

public record PaiementResponse(
        UUID id,
        UUID commandeId,
        UUID producteurId,
        BigDecimal montantXof,
        LocalDate dateCreation,
        StatutPaiement statut,
        boolean smsEnvoye) {

    static PaiementResponse depuis(PaiementDu paiement) {
        return new PaiementResponse(
                paiement.id(),
                paiement.commandeId(),
                paiement.producteurId(),
                paiement.montantXof(),
                paiement.dateCreation(),
                paiement.statut(),
                paiement.smsEnvoye());
    }
}
