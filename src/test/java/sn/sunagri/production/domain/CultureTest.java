package sn.sunagri.production.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;

class CultureTest {

    private static final ParametresCulture PARAMETRES =
            new ParametresCulture(BigDecimal.valueOf(1200), BigDecimal.valueOf(9), 90);

    private Culture culture() {
        return Culture.planifier(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TypeCulture.ARACHIDE, BigDecimal.valueOf(2), PARAMETRES);
    }

    @Test
    void une_culture_declaree_est_planifiee() {
        assertThat(culture().statut()).isEqualTo(StatutCulture.PLANIFIEE);
    }

    @Test
    void suit_l_ordre_planifiee_semee_recoltee() {
        Culture culture = culture();
        LocalDate semis = LocalDate.of(2026, 6, 1);

        culture.semer(semis);
        culture.recolter(semis.plusDays(90), BigDecimal.valueOf(2400));

        assertThat(culture.statut()).isEqualTo(StatutCulture.RECOLTEE);
        assertThat(culture.quantiteRecoltee()).isEqualByComparingTo("2400");
    }

    @Test
    void refuse_une_recolte_avant_le_cycle_minimal() {
        Culture culture = culture();
        LocalDate semis = LocalDate.of(2026, 6, 1);
        culture.semer(semis);

        assertThatThrownBy(() -> culture.recolter(semis.plusDays(89), BigDecimal.valueOf(1000)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("cycle minimal");
    }

    @Test
    void refuse_un_rendement_superieur_au_double_de_la_reference() {
        Culture culture = culture();
        LocalDate semis = LocalDate.of(2026, 6, 1);
        culture.semer(semis);

        assertThatThrownBy(() -> culture.recolter(semis.plusDays(90), BigDecimal.valueOf(4801)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("rendement");
    }

    @Test
    void refuse_une_seconde_recolte() {
        Culture culture = culture();
        LocalDate semis = LocalDate.of(2026, 6, 1);
        culture.semer(semis);
        culture.recolter(semis.plusDays(90), BigDecimal.valueOf(2400));

        assertThatThrownBy(() -> culture.recolter(semis.plusDays(100), BigDecimal.valueOf(1000)))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void refuse_une_recolte_sans_semis() {
        assertThatThrownBy(() -> culture().recolter(LocalDate.of(2026, 9, 1), BigDecimal.valueOf(1000)))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void refuse_une_quantite_recoltee_nulle() {
        Culture culture = culture();
        LocalDate semis = LocalDate.of(2026, 6, 1);
        culture.semer(semis);

        assertThatThrownBy(() -> culture.recolter(semis.plusDays(90), BigDecimal.ZERO))
                .isInstanceOf(RegleMetierException.class);
    }
}
