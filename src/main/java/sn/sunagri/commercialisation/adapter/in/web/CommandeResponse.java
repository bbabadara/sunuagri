package sn.sunagri.commercialisation.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.StatutCommande;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

public record CommandeResponse(
        UUID id,
        UUID offreId,
        String acheteurId,
        TypeCulture typeCulture,
        Qualite qualite,
        BigDecimal quantiteKg,
        BigDecimal prixKg,
        BigDecimal montantXof,
        LocalDate dateCommande,
        StatutCommande statut) {

    static CommandeResponse depuis(Commande commande) {
        return new CommandeResponse(
                commande.id(),
                commande.offreId(),
                commande.acheteurId(),
                commande.typeCulture(),
                commande.qualite(),
                commande.quantiteKg(),
                commande.prixKg(),
                commande.montantXof(),
                commande.dateCommande(),
                commande.statut());
    }
}
