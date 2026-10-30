package sn.sunagri.entrepot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;

class LotTest {

    private Lot lot() {
        return Lot.creer(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000), LocalDate.of(2026, 10, 1));
    }

    @Test
    void consomme_partiellement_puis_totalement() {
        Lot lot = lot();

        lot.consommer(BigDecimal.valueOf(400));

        assertThat(lot.quantiteDisponible()).isEqualByComparingTo("600");
        assertThat(lot.estEpuise()).isFalse();

        lot.consommer(BigDecimal.valueOf(600));

        assertThat(lot.quantiteDisponible()).isEqualByComparingTo("0");
        assertThat(lot.estEpuise()).isTrue();
    }

    @Test
    void refuse_de_consommer_plus_que_disponible() {
        Lot lot = lot();

        assertThatThrownBy(() -> lot.consommer(BigDecimal.valueOf(1001)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("1000");

        assertThat(lot.quantiteDisponible()).isEqualByComparingTo("1000");
    }
}
