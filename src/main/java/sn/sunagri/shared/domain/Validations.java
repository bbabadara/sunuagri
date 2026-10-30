package sn.sunagri.shared.domain;

import java.math.BigDecimal;

public final class Validations {

    private Validations() {
    }

    public static String texteObligatoire(String valeur, String champ) {
        if (valeur == null || valeur.isBlank()) {
            throw new RegleMetierException("CHAMP_OBLIGATOIRE", "Le champ « " + champ + " » est obligatoire.");
        }
        return valeur.trim();
    }

    public static BigDecimal strictementPositif(BigDecimal valeur, String champ) {
        if (valeur == null || valeur.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegleMetierException("VALEUR_NON_POSITIVE",
                    "Le champ « " + champ + " » doit être strictement positif.");
        }
        return valeur;
    }

    public static BigDecimal positifOuNul(BigDecimal valeur, String champ) {
        if (valeur == null || valeur.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegleMetierException("VALEUR_NEGATIVE",
                    "Le champ « " + champ + " » ne peut pas être négatif.");
        }
        return valeur;
    }
}
