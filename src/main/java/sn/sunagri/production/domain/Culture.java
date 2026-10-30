package sn.sunagri.production.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

public class Culture {

    private static final BigDecimal PLAFOND_RENDEMENT = BigDecimal.valueOf(2);

    private final UUID id;
    private final UUID parcelleId;
    private final UUID producteurId;
    private final UUID campagneId;
    private final TypeCulture type;
    private final BigDecimal superficieEmblavee;
    private final ParametresCulture parametres;
    private StatutCulture statut;
    private LocalDate dateSemis;
    private LocalDate dateRecolte;
    private BigDecimal quantiteRecoltee;

    private Culture(UUID id, UUID parcelleId, UUID producteurId, UUID campagneId, TypeCulture type,
            BigDecimal superficieEmblavee, ParametresCulture parametres) {
        if (id == null || parcelleId == null || producteurId == null || campagneId == null) {
            throw new IllegalArgumentException("Les identifiants de la culture sont obligatoires.");
        }
        if (type == null) {
            throw new RegleMetierException("CULTURE_INCONNUE", "Le type de culture est obligatoire.");
        }
        this.id = id;
        this.parcelleId = parcelleId;
        this.producteurId = producteurId;
        this.campagneId = campagneId;
        this.type = type;
        this.superficieEmblavee = Validations.strictementPositif(superficieEmblavee, "superficie emblavée");
        this.parametres = parametres;
        this.statut = StatutCulture.PLANIFIEE;
    }

    public static Culture planifier(UUID id, UUID parcelleId, UUID producteurId, UUID campagneId,
            TypeCulture type, BigDecimal superficieEmblavee, ParametresCulture parametres) {
        return new Culture(id, parcelleId, producteurId, campagneId, type, superficieEmblavee, parametres);
    }

    public void semer(LocalDate date) {
        if (statut != StatutCulture.PLANIFIEE) {
            throw new RegleMetierException("TRANSITION_INVALIDE",
                    "Seule une culture planifiée peut être semée.");
        }
        if (date == null) {
            throw new RegleMetierException("DATE_SEMIS_OBLIGATOIRE", "La date de semis est obligatoire.");
        }
        this.dateSemis = date;
        this.statut = StatutCulture.SEMEE;
    }

    public void recolter(LocalDate date, BigDecimal quantiteKg) {
        if (statut != StatutCulture.SEMEE) {
            throw new RegleMetierException("TRANSITION_INVALIDE",
                    "Seule une culture semée et non encore récoltée peut être récoltée.");
        }
        if (date == null) {
            throw new RegleMetierException("DATE_RECOLTE_OBLIGATOIRE", "La date de récolte est obligatoire.");
        }
        Validations.strictementPositif(quantiteKg, "quantité récoltée");
        LocalDate dateMinimale = dateSemis.plusDays(parametres.cycleMinJours());
        if (date.isBefore(dateMinimale)) {
            throw new RegleMetierException("CYCLE_MINIMAL_NON_RESPECTE",
                    "La récolte ne peut pas avoir lieu avant le " + dateMinimale
                            + " (cycle minimal de " + parametres.cycleMinJours() + " jours).");
        }
        BigDecimal rendement = quantiteKg.divide(superficieEmblavee, 4, RoundingMode.HALF_UP);
        BigDecimal rendementMaximal = parametres.rendementReferenceKgHa().multiply(PLAFOND_RENDEMENT);
        if (rendement.compareTo(rendementMaximal) > 0) {
            throw new RegleMetierException("RENDEMENT_SUSPECT",
                    "Le rendement de " + rendement.toPlainString() + " kg/ha dépasse le double du rendement de référence ("
                            + rendementMaximal.toPlainString() + " kg/ha).");
        }
        this.dateRecolte = date;
        this.quantiteRecoltee = quantiteKg;
        this.statut = StatutCulture.RECOLTEE;
    }

    public boolean estRecoltee() {
        return statut == StatutCulture.RECOLTEE;
    }

    public UUID id() {
        return id;
    }

    public UUID parcelleId() {
        return parcelleId;
    }

    public UUID producteurId() {
        return producteurId;
    }

    public UUID campagneId() {
        return campagneId;
    }

    public TypeCulture type() {
        return type;
    }

    public BigDecimal superficieEmblavee() {
        return superficieEmblavee;
    }

    public ParametresCulture parametres() {
        return parametres;
    }

    public StatutCulture statut() {
        return statut;
    }

    public LocalDate dateSemis() {
        return dateSemis;
    }

    public LocalDate dateRecolte() {
        return dateRecolte;
    }

    public BigDecimal quantiteRecoltee() {
        return quantiteRecoltee;
    }
}
