package sn.sunagri.production.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.sunagri.shared.domain.ConflitException;
import sn.sunagri.shared.domain.RegleMetierException;

class ProducteurTest {

    private static final BigDecimal SUPERFICIE_MAX = BigDecimal.valueOf(50);

    private Producteur producteur() {
        return Producteur.creer(UUID.randomUUID(), "Diop", "Awa", "771234567", "Thiès");
    }

    @Test
    void declare_une_parcelle_valide() {
        Producteur producteur = producteur();

        Parcelle parcelle = producteur.ajouterParcelle(UUID.randomUUID(), "Champ Nord",
                BigDecimal.valueOf(10), "Thiès", SUPERFICIE_MAX);

        assertThat(parcelle.nom()).isEqualTo("Champ Nord");
        assertThat(producteur.parcelles()).hasSize(1);
        assertThat(producteur.superficieTotale()).isEqualByComparingTo("10");
    }

    @Test
    void refuse_une_superficie_negative() {
        Producteur producteur = producteur();

        assertThatThrownBy(() -> producteur.ajouterParcelle(UUID.randomUUID(), "Champ",
                BigDecimal.valueOf(-1), "Thiès", SUPERFICIE_MAX))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void refuse_deux_parcelles_portant_le_meme_nom() {
        Producteur producteur = producteur();
        producteur.ajouterParcelle(UUID.randomUUID(), "Champ Nord", BigDecimal.ONE, "Thiès", SUPERFICIE_MAX);

        assertThatThrownBy(() -> producteur.ajouterParcelle(UUID.randomUUID(), "champ nord",
                BigDecimal.ONE, "Thiès", SUPERFICIE_MAX))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void refuse_de_depasser_la_superficie_maximale() {
        Producteur producteur = producteur();
        producteur.ajouterParcelle(UUID.randomUUID(), "Champ 1", BigDecimal.valueOf(30), "Thiès", SUPERFICIE_MAX);

        assertThatThrownBy(() -> producteur.ajouterParcelle(UUID.randomUUID(), "Champ 2",
                BigDecimal.valueOf(21), "Thiès", SUPERFICIE_MAX))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("50");
    }

    @Test
    void accepte_exactement_la_superficie_maximale() {
        Producteur producteur = producteur();
        producteur.ajouterParcelle(UUID.randomUUID(), "Champ 1", BigDecimal.valueOf(30), "Thiès", SUPERFICIE_MAX);

        producteur.ajouterParcelle(UUID.randomUUID(), "Champ 2", BigDecimal.valueOf(20), "Thiès", SUPERFICIE_MAX);

        assertThat(producteur.superficieTotale()).isEqualByComparingTo("50");
    }
}
