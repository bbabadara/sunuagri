package sn.sunagri.commercialisation.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.Validations;

public class PaiementDu {

    private final UUID id;
    private final UUID commandeId;
    private final UUID producteurId;
    private final BigDecimal montantXof;
    private final LocalDate dateCreation;
    private StatutPaiement statut;
    private boolean smsEnvoye;

    private PaiementDu(UUID id, UUID commandeId, UUID producteurId, BigDecimal montantXof, LocalDate dateCreation) {
        if (id == null || commandeId == null || producteurId == null) {
            throw new IllegalArgumentException("Les identifiants du paiement sont obligatoires.");
        }
        if (dateCreation == null) {
            throw new IllegalArgumentException("La date de création du paiement est obligatoire.");
        }
        this.id = id;
        this.commandeId = commandeId;
        this.producteurId = producteurId;
        this.montantXof = Validations.strictementPositif(montantXof, "montant du paiement");
        this.dateCreation = dateCreation;
        this.statut = StatutPaiement.A_PAYER;
        this.smsEnvoye = false;
    }

    public static PaiementDu creer(UUID id, UUID commandeId, UUID producteurId, BigDecimal montantXof,
            LocalDate dateCreation) {
        return new PaiementDu(id, commandeId, producteurId, montantXof, dateCreation);
    }

    public void marquerSmsEnvoye() {
        this.smsEnvoye = true;
    }

    public void marquerPaye() {
        if (statut == StatutPaiement.PAYE) {
            throw new RegleMetierException("PAIEMENT_DEJA_PAYE", "Ce paiement est déjà marqué comme payé.");
        }
        this.statut = StatutPaiement.PAYE;
    }

    public UUID id() {
        return id;
    }

    public UUID commandeId() {
        return commandeId;
    }

    public UUID producteurId() {
        return producteurId;
    }

    public BigDecimal montantXof() {
        return montantXof;
    }

    public LocalDate dateCreation() {
        return dateCreation;
    }

    public StatutPaiement statut() {
        return statut;
    }

    public boolean smsEnvoye() {
        return smsEnvoye;
    }
}
