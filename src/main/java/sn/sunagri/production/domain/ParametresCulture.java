package sn.sunagri.production.domain;

import java.math.BigDecimal;
import sn.sunagri.shared.domain.Validations;

/**
 * Instantané des paramètres de référence d'une culture, capturé à la déclaration.
 * Les modifications ultérieures du référentiel ne s'appliquent pas aux cultures déjà
 * déclarées (US-19).
 */
public record ParametresCulture(
        BigDecimal rendementReferenceKgHa,
        BigDecimal humiditeMax,
        int cycleMinJours) {

    public ParametresCulture {
        Validations.strictementPositif(rendementReferenceKgHa, "rendement de référence");
        Validations.strictementPositif(humiditeMax, "humidité maximale");
        if (cycleMinJours <= 0) {
            throw new IllegalArgumentException("Le cycle minimal doit être strictement positif.");
        }
    }
}
