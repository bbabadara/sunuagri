package sn.sunagri.commercialisation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;

class CommandeTest {

    @Test
    void le_montant_est_arrondi_au_franc_pres() {
        Commande commande = commande(BigDecimal.valueOf(55), new BigDecimal("301.5"));

        assertThat(commande.montantXof()).isEqualByComparingTo("16583");
        assertThat(commande.statut()).isEqualTo(StatutCommande.EN_ATTENTE);
    }

    @Test
    void une_commande_livree_ne_peut_pas_etre_annulee() {
        Commande commande = commande(BigDecimal.valueOf(100), BigDecimal.valueOf(300));
        commande.livrer();

        assertThatThrownBy(commande::annuler)
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("livrée");
    }

    @Test
    void une_commande_annulee_ne_peut_pas_etre_livree() {
        Commande commande = commande(BigDecimal.valueOf(100), BigDecimal.valueOf(300));
        commande.annuler();

        assertThatThrownBy(commande::livrer)
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("en attente");
    }

    @Test
    void une_commande_ne_peut_etre_annulee_qu_une_fois() {
        Commande commande = commande(BigDecimal.valueOf(100), BigDecimal.valueOf(300));
        commande.annuler();

        assertThatThrownBy(commande::annuler)
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("déjà annulée");
    }

    private Commande commande(BigDecimal quantite, BigDecimal prix) {
        return Commande.creer(UUID.randomUUID(), UUID.randomUUID(), "acheteur:test", TypeCulture.ARACHIDE,
                Qualite.A, quantite, prix, LocalDate.of(2026, 10, 1));
    }
}
