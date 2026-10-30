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

class OffreTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);

    @Test
    void une_offre_publiee_reserve_toute_sa_quantite() {
        Offre offre = offre(1000);

        assertThat(offre.statut()).isEqualTo(StatutOffre.OUVERTE);
        assertThat(offre.quantiteRestante()).isEqualByComparingTo("1000");
        assertThat(offre.quantiteReservee()).isEqualByComparingTo("1000");
    }

    @Test
    void une_commande_partielle_laisse_l_offre_ouverte() {
        Offre offre = offre(1000);

        offre.commander(BigDecimal.valueOf(400));

        assertThat(offre.statut()).isEqualTo(StatutOffre.OUVERTE);
        assertThat(offre.quantiteRestante()).isEqualByComparingTo("600");
        assertThat(offre.quantiteReservee()).isEqualByComparingTo("1000");
    }

    @Test
    void une_commande_qui_epuise_l_offre_la_ferme() {
        Offre offre = offre(1000);

        offre.commander(BigDecimal.valueOf(1000));

        assertThat(offre.statut()).isEqualTo(StatutOffre.EPUISEE);
        assertThat(offre.quantiteRestante()).isEqualByComparingTo("0");
        assertThatThrownBy(() -> offre.commander(BigDecimal.ONE))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("ouverte");
    }

    @Test
    void une_commande_superieure_a_la_quantite_restante_est_refusee() {
        Offre offre = offre(1000);

        assertThatThrownBy(() -> offre.commander(BigDecimal.valueOf(1001)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("kg");
        assertThat(offre.quantiteRestante()).isEqualByComparingTo("1000");
    }

    @Test
    void annuler_une_commande_avant_retrait_rend_la_quantite_disponible() {
        Offre offre = offre(1000);
        offre.commander(BigDecimal.valueOf(1000));

        offre.annulerCommande(BigDecimal.valueOf(400));

        assertThat(offre.statut()).isEqualTo(StatutOffre.OUVERTE);
        assertThat(offre.quantiteRestante()).isEqualByComparingTo("400");
        assertThat(offre.quantiteReservee()).isEqualByComparingTo("1000");
    }

    @Test
    void retirer_une_offre_libere_la_quantite_non_vendue() {
        Offre offre = offre(1000);
        offre.commander(BigDecimal.valueOf(300));

        offre.retirer();

        assertThat(offre.statut()).isEqualTo(StatutOffre.RETIREE);
        assertThat(offre.quantiteRetiree()).isEqualByComparingTo("700");
        assertThat(offre.quantiteReservee()).isEqualByComparingTo("300");
    }

    @Test
    void annuler_apres_retrait_libere_la_quantite_commandee() {
        Offre offre = offre(1000);
        offre.commander(BigDecimal.valueOf(300));
        offre.retirer();

        offre.annulerCommande(BigDecimal.valueOf(300));

        assertThat(offre.quantiteRetiree()).isEqualByComparingTo("1000");
        assertThat(offre.quantiteReservee()).isEqualByComparingTo("0");
    }

    @Test
    void une_livraison_ne_peut_pas_depasser_la_quantite_vendue() {
        Offre offre = offre(1000);
        offre.commander(BigDecimal.valueOf(1000));

        assertThatThrownBy(() -> offre.enregistrerLivraison(BigDecimal.valueOf(1001)))
                .isInstanceOf(RegleMetierException.class);
        assertThat(offre.quantiteLivree()).isEqualByComparingTo("0");
    }

    @Test
    void apres_retrait_les_commandes_en_attente_restent_livrables() {
        Offre offre = offre(1000);
        offre.commander(BigDecimal.valueOf(300));
        offre.retirer();

        offre.enregistrerLivraison(BigDecimal.valueOf(300));

        assertThat(offre.quantiteLivree()).isEqualByComparingTo("300");
        assertThat(offre.quantiteReservee()).isEqualByComparingTo("0");
    }

    private Offre offre(long quantite) {
        return Offre.publier(UUID.randomUUID(), TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(quantite),
                BigDecimal.valueOf(300), DATE);
    }
}
