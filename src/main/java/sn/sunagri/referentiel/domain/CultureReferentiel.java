package sn.sunagri.referentiel.domain;

import java.math.BigDecimal;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

public record CultureReferentiel(
        TypeCulture type,
        String libelle,
        BigDecimal rendementKgHa,
        BigDecimal humiditeMax,
        int cycleMinJours) {

    public CultureReferentiel {
        if (type == null) {
            throw new IllegalArgumentException("Le type de culture est obligatoire.");
        }
        libelle = Validations.texteObligatoire(libelle, "libellé");
        Validations.strictementPositif(rendementKgHa, "rendement de référence");
        Validations.strictementPositif(humiditeMax, "humidité maximale");
        if (cycleMinJours <= 0) {
            throw new IllegalArgumentException("Le cycle minimal doit être strictement positif.");
        }
    }
}
