package sn.sunagri.production.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.sunagri.shared.domain.RegleMetierException;

class CampagneTest {

    private Campagne campagne() {
        return Campagne.ouvrir(UUID.randomUUID(), Saison.HIVERNAGE, 2026,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 10, 31));
    }

    @Test
    void ouvre_une_campagne() {
        assertThat(campagne().statut()).isEqualTo(StatutCampagne.OUVERTE);
    }

    @Test
    void refuse_une_date_de_fin_avant_la_date_de_debut() {
        assertThatThrownBy(() -> Campagne.ouvrir(UUID.randomUUID(), Saison.HIVERNAGE, 2026,
                LocalDate.of(2026, 10, 31), LocalDate.of(2026, 6, 1)))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void cloture_une_campagne_et_refuse_les_nouvelles_cultures() {
        Campagne campagne = campagne();
        campagne.cloturer();

        assertThat(campagne.statut()).isEqualTo(StatutCampagne.CLOTUREE);
        assertThatThrownBy(campagne::exigerOuverte).isInstanceOf(RegleMetierException.class);
    }

    @Test
    void refuse_une_double_cloture() {
        Campagne campagne = campagne();
        campagne.cloturer();

        assertThatThrownBy(campagne::cloturer).isInstanceOf(RegleMetierException.class);
    }
}
