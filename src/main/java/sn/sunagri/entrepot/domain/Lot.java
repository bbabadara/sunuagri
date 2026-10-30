package sn.sunagri.entrepot.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

public class Lot {

    private final UUID id;
    private final UUID livraisonId;
    private final UUID producteurId;
    private final TypeCulture typeCulture;
    private final Qualite qualite;
    private final BigDecimal quantiteInitiale;
    private final LocalDate dateReception;
    private BigDecimal quantiteDisponible;

    private Lot(UUID id, UUID livraisonId, UUID producteurId, TypeCulture typeCulture, Qualite qualite,
            BigDecimal quantiteInitiale, LocalDate dateReception) {
        if (id == null || livraisonId == null || producteurId == null) {
            throw new IllegalArgumentException("Les identifiants du lot sont obligatoires.");
        }
        if (typeCulture == null || qualite == null) {
            throw new IllegalArgumentException("La culture et la qualité du lot sont obligatoires.");
        }
        if (dateReception == null) {
            throw new IllegalArgumentException("La date de réception du lot est obligatoire.");
        }
        this.id = id;
        this.livraisonId = livraisonId;
        this.producteurId = producteurId;
        this.typeCulture = typeCulture;
        this.qualite = qualite;
        this.quantiteInitiale = Validations.strictementPositif(quantiteInitiale, "quantité du lot");
        this.quantiteDisponible = this.quantiteInitiale;
        this.dateReception = dateReception;
    }

    public static Lot creer(UUID id, UUID livraisonId, UUID producteurId, TypeCulture typeCulture,
            Qualite qualite, BigDecimal quantiteKg, LocalDate dateReception) {
        return new Lot(id, livraisonId, producteurId, typeCulture, qualite, quantiteKg, dateReception);
    }

    public void consommer(BigDecimal quantiteKg) {
        Validations.strictementPositif(quantiteKg, "quantité à consommer");
        if (quantiteKg.compareTo(quantiteDisponible) > 0) {
            throw new RegleMetierException("STOCK_INSUFFISANT",
                    "Le lot ne contient que " + quantiteDisponible.toPlainString() + " kg disponibles.");
        }
        quantiteDisponible = quantiteDisponible.subtract(quantiteKg);
    }

    public boolean estEpuise() {
        return quantiteDisponible.signum() == 0;
    }

    public UUID id() {
        return id;
    }

    public UUID livraisonId() {
        return livraisonId;
    }

    public UUID producteurId() {
        return producteurId;
    }

    public TypeCulture typeCulture() {
        return typeCulture;
    }

    public Qualite qualite() {
        return qualite;
    }

    public BigDecimal quantiteInitiale() {
        return quantiteInitiale;
    }

    public BigDecimal quantiteDisponible() {
        return quantiteDisponible;
    }

    public LocalDate dateReception() {
        return dateReception;
    }
}
