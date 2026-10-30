package sn.sunagri.production.domain;

import java.math.BigDecimal;
import java.util.UUID;
import sn.sunagri.shared.domain.Validations;

public record Parcelle(UUID id, String nom, BigDecimal superficie, String commune) {

    public Parcelle {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant de la parcelle est obligatoire.");
        }
        nom = Validations.texteObligatoire(nom, "nom de la parcelle");
        Validations.strictementPositif(superficie, "superficie");
        commune = Validations.texteObligatoire(commune, "commune");
    }
}
