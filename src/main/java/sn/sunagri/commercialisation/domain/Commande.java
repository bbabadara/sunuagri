package sn.sunagri.commercialisation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

public class Commande {

    private final UUID id;
    private final UUID offreId;
    private final String acheteurId;
    private final TypeCulture typeCulture;
    private final Qualite qualite;
    private final BigDecimal quantiteKg;
    private final BigDecimal prixKg;
    private final BigDecimal montantXof;
    private final LocalDate dateCommande;
    private StatutCommande statut;

    private Commande(UUID id, UUID offreId, String acheteurId, TypeCulture typeCulture, Qualite qualite,
            BigDecimal quantiteKg, BigDecimal prixKg, LocalDate dateCommande) {
        if (id == null || offreId == null) {
            throw new IllegalArgumentException("Les identifiants de la commande sont obligatoires.");
        }
        if (acheteurId == null || acheteurId.isBlank()) {
            throw new IllegalArgumentException("L'acheteur est obligatoire.");
        }
        if (typeCulture == null || qualite == null) {
            throw new RegleMetierException("COMMANDE_INVALIDE", "La culture et la qualité sont obligatoires.");
        }
        if (dateCommande == null) {
            throw new IllegalArgumentException("La date de commande est obligatoire.");
        }
        this.id = id;
        this.offreId = offreId;
        this.acheteurId = acheteurId;
        this.typeCulture = typeCulture;
        this.qualite = qualite;
        this.quantiteKg = Validations.strictementPositif(quantiteKg, "quantité commandée");
        this.prixKg = Validations.strictementPositif(prixKg, "prix au kilogramme");
        this.montantXof = this.quantiteKg.multiply(this.prixKg).setScale(0, RoundingMode.HALF_UP);
        this.dateCommande = dateCommande;
        this.statut = StatutCommande.EN_ATTENTE;
    }

    public static Commande creer(UUID id, UUID offreId, String acheteurId, TypeCulture typeCulture, Qualite qualite,
            BigDecimal quantiteKg, BigDecimal prixKg, LocalDate dateCommande) {
        return new Commande(id, offreId, acheteurId, typeCulture, qualite, quantiteKg, prixKg, dateCommande);
    }

    public void annuler() {
        if (statut == StatutCommande.LIVREE) {
            throw new RegleMetierException("COMMANDE_LIVREE",
                    "Une commande livrée ne peut pas être annulée.");
        }
        if (statut == StatutCommande.ANNULEE) {
            throw new RegleMetierException("COMMANDE_DEJA_ANNULEE", "Cette commande est déjà annulée.");
        }
        statut = StatutCommande.ANNULEE;
    }

    public void livrer() {
        if (statut != StatutCommande.EN_ATTENTE) {
            throw new RegleMetierException("COMMANDE_NON_LIVRABLE",
                    "Seule une commande en attente peut être livrée.");
        }
        statut = StatutCommande.LIVREE;
    }

    public boolean estEnAttente() {
        return statut == StatutCommande.EN_ATTENTE;
    }

    public UUID id() {
        return id;
    }

    public UUID offreId() {
        return offreId;
    }

    public String acheteurId() {
        return acheteurId;
    }

    public TypeCulture typeCulture() {
        return typeCulture;
    }

    public Qualite qualite() {
        return qualite;
    }

    public BigDecimal quantiteKg() {
        return quantiteKg;
    }

    public BigDecimal prixKg() {
        return prixKg;
    }

    public BigDecimal montantXof() {
        return montantXof;
    }

    public LocalDate dateCommande() {
        return dateCommande;
    }

    public StatutCommande statut() {
        return statut;
    }
}
