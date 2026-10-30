package sn.sunagri.commercialisation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.sunagri.shared.domain.RegleMetierException;

class RepartitionPaiementTest {

    private static final UUID PRODUCTEUR_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCTEUR_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID PRODUCTEUR_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void retient_la_commission_avant_de_repartir_au_prorata() {
        ResultatRepartition resultat = RepartitionPaiement.calculer(BigDecimal.valueOf(10_000),
                new BigDecimal("0.05"), Map.of(PRODUCTEUR_1, BigDecimal.valueOf(100),
                        PRODUCTEUR_2, BigDecimal.valueOf(100)));

        assertThat(resultat.commissionXof()).isEqualByComparingTo("500");
        assertThat(resultat.montantsParProducteur()).containsEntry(PRODUCTEUR_1, BigDecimal.valueOf(4750))
                .containsEntry(PRODUCTEUR_2, BigDecimal.valueOf(4750));
    }

    @Test
    void attribue_les_francs_restants_aux_plus_forts_restes_avec_departage_par_identifiant() {
        ResultatRepartition resultat = RepartitionPaiement.calculer(BigDecimal.valueOf(1000), BigDecimal.ZERO,
                Map.of(PRODUCTEUR_1, BigDecimal.ONE, PRODUCTEUR_2, BigDecimal.ONE,
                        PRODUCTEUR_3, BigDecimal.ONE));

        assertThat(resultat.montantsParProducteur()).containsEntry(PRODUCTEUR_1, BigDecimal.valueOf(334))
                .containsEntry(PRODUCTEUR_2, BigDecimal.valueOf(333))
                .containsEntry(PRODUCTEUR_3, BigDecimal.valueOf(333));
        assertThat(somme(resultat)).isEqualByComparingTo("1000");
    }

    @Test
    void la_somme_des_parts_et_de_la_commission_est_toujours_egale_au_montant() {
        Map<UUID, BigDecimal> quantites = new LinkedHashMap<>();
        quantites.put(PRODUCTEUR_1, BigDecimal.valueOf(7));
        quantites.put(PRODUCTEUR_2, BigDecimal.valueOf(11));
        quantites.put(PRODUCTEUR_3, BigDecimal.valueOf(13));

        ResultatRepartition resultat = RepartitionPaiement.calculer(BigDecimal.valueOf(100_001),
                new BigDecimal("0.05"), quantites);

        assertThat(resultat.commissionXof()).isEqualByComparingTo("5000");
        assertThat(somme(resultat)).isEqualByComparingTo("100001");
    }

    @Test
    void refuse_une_repartition_sans_quantite() {
        assertThatThrownBy(() -> RepartitionPaiement.calculer(BigDecimal.TEN, new BigDecimal("0.05"),
                Map.of(PRODUCTEUR_1, BigDecimal.ZERO)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("quantité");
    }

    private BigDecimal somme(ResultatRepartition resultat) {
        return resultat.montantsParProducteur().values().stream()
                .reduce(resultat.commissionXof(), BigDecimal::add);
    }
}
