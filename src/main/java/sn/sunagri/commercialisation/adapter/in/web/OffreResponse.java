package sn.sunagri.commercialisation.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.commercialisation.domain.Offre;
import sn.sunagri.commercialisation.domain.StatutOffre;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

public record OffreResponse(
        UUID id,
        TypeCulture typeCulture,
        Qualite qualite,
        BigDecimal quantiteInitialeKg,
        BigDecimal quantiteRestanteKg,
        BigDecimal quantiteLivreeKg,
        BigDecimal quantiteRetireeKg,
        BigDecimal prixKg,
        LocalDate dateCreation,
        StatutOffre statut) {

    static OffreResponse depuis(Offre offre) {
        return new OffreResponse(
                offre.id(),
                offre.typeCulture(),
                offre.qualite(),
                offre.quantiteInitiale(),
                offre.quantiteRestante(),
                offre.quantiteLivree(),
                offre.quantiteRetiree(),
                offre.prixKg(),
                offre.dateCreation(),
                offre.statut());
    }
}
