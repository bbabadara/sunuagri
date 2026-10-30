package sn.sunagri.commercialisation.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

public class Offre {

    private final UUID id;
    private final TypeCulture typeCulture;
    private final Qualite qualite;
    private final BigDecimal quantiteInitiale;
    private final BigDecimal prixKg;
    private final LocalDate dateCreation;
    private BigDecimal quantiteRestante;
    private BigDecimal quantiteLivree;
    private BigDecimal quantiteRetiree;
    private StatutOffre statut;

    private Offre(UUID id, TypeCulture typeCulture, Qualite qualite, BigDecimal quantiteInitiale, BigDecimal prixKg,
            LocalDate dateCreation) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant de l'offre est obligatoire.");
        }
        if (typeCulture == null || qualite == null) {
            throw new RegleMetierException("OFFRE_INVALIDE", "La culture et la qualité sont obligatoires.");
        }
        if (dateCreation == null) {
            throw new IllegalArgumentException("La date de création de l'offre est obligatoire.");
        }
        this.id = id;
        this.typeCulture = typeCulture;
        this.qualite = qualite;
        this.quantiteInitiale = Validations.strictementPositif(quantiteInitiale, "quantité de l'offre");
        this.prixKg = Validations.strictementPositif(prixKg, "prix au kilogramme");
        this.dateCreation = dateCreation;
        this.quantiteRestante = quantiteInitiale;
        this.quantiteLivree = BigDecimal.ZERO;
        this.quantiteRetiree = BigDecimal.ZERO;
        this.statut = StatutOffre.OUVERTE;
    }

    public static Offre publier(UUID id, TypeCulture typeCulture, Qualite qualite, BigDecimal quantiteKg,
            BigDecimal prixKg, LocalDate dateCreation) {
        return new Offre(id, typeCulture, qualite, quantiteKg, prixKg, dateCreation);
    }

    public void commander(BigDecimal quantiteKg) {
        Validations.strictementPositif(quantiteKg, "quantité commandée");
        if (statut != StatutOffre.OUVERTE) {
            throw new RegleMetierException("OFFRE_NON_OUVERTE", "Cette offre n'est pas ouverte à la commande.");
        }
        if (quantiteKg.compareTo(quantiteRestante) > 0) {
            throw new RegleMetierException("QUANTITE_SUPERIEURE_A_L_OFFRE",
                    "Il ne reste que " + quantiteRestante.toPlainString() + " kg sur cette offre.");
        }
        quantiteRestante = quantiteRestante.subtract(quantiteKg);
        if (quantiteRestante.signum() == 0) {
            statut = StatutOffre.EPUISEE;
        }
    }

    public void annulerCommande(BigDecimal quantiteKg) {
        Validations.strictementPositif(quantiteKg, "quantité annulée");
        if (statut == StatutOffre.RETIREE) {
            quantiteRetiree = quantiteRetiree.add(quantiteKg);
            return;
        }
        quantiteRestante = quantiteRestante.add(quantiteKg);
        if (statut == StatutOffre.EPUISEE) {
            statut = StatutOffre.OUVERTE;
        }
    }

    public void retirer() {
        if (statut == StatutOffre.RETIREE) {
            throw new RegleMetierException("OFFRE_DEJA_RETIREE", "Cette offre est déjà retirée.");
        }
        quantiteRetiree = quantiteRetiree.add(quantiteRestante);
        quantiteRestante = BigDecimal.ZERO;
        statut = StatutOffre.RETIREE;
    }

    public void enregistrerLivraison(BigDecimal quantiteKg) {
        Validations.strictementPositif(quantiteKg, "quantité livrée");
        BigDecimal livrableMax = quantiteInitiale.subtract(quantiteRetiree);
        if (quantiteLivree.add(quantiteKg).compareTo(livrableMax) > 0) {
            throw new RegleMetierException("LIVRAISON_SUPERIEURE_A_LA_COMMANDE",
                    "La quantité livrée dépasserait la quantité vendue de cette offre.");
        }
        quantiteLivree = quantiteLivree.add(quantiteKg);
    }

    /**
     * Quantité encore engagée : commandes en attente non livrées, hors quantités
     * retirées de la vente.
     */
    public BigDecimal quantiteReservee() {
        return quantiteInitiale.subtract(quantiteLivree).subtract(quantiteRetiree).max(BigDecimal.ZERO);
    }

    public boolean estOuverte() {
        return statut == StatutOffre.OUVERTE;
    }

    public UUID id() {
        return id;
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

    public BigDecimal quantiteRestante() {
        return quantiteRestante;
    }

    public BigDecimal quantiteLivree() {
        return quantiteLivree;
    }

    public BigDecimal quantiteRetiree() {
        return quantiteRetiree;
    }

    public BigDecimal prixKg() {
        return prixKg;
    }

    public LocalDate dateCreation() {
        return dateCreation;
    }

    public StatutOffre statut() {
        return statut;
    }
}
