package sn.sunagri.entrepot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

public record Livraison(
        UUID id,
        UUID cultureId,
        UUID producteurId,
        TypeCulture typeCulture,
        Qualite qualite,
        BigDecimal quantiteKg,
        BigDecimal tauxHumidite,
        LocalDate dateLivraison,
        UUID lotId) {

    public Livraison {
        if (id == null || cultureId == null || producteurId == null || lotId == null) {
            throw new IllegalArgumentException("Les identifiants de la livraison sont obligatoires.");
        }
        if (typeCulture == null || qualite == null) {
            throw new IllegalArgumentException("La culture et la qualité sont obligatoires.");
        }
        if (dateLivraison == null) {
            throw new IllegalArgumentException("La date de livraison est obligatoire.");
        }
        Validations.strictementPositif(quantiteKg, "quantité livrée");
        Validations.positifOuNul(tauxHumidite, "taux d'humidité");
    }
}
