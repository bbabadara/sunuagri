package sn.sunagri.entrepot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.sunagri.entrepot.adapter.out.memory.InMemoryLotRepository;
import sn.sunagri.entrepot.application.service.StockService;
import sn.sunagri.entrepot.domain.Lot;
import sn.sunagri.entrepot.domain.LotConsomme;
import sn.sunagri.entrepot.domain.StockLigne;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;

class StockServiceTest {

    private InMemoryLotRepository lots;
    private StockService service;
    private Utilisateur agent;

    @BeforeEach
    void setUp() {
        lots = new InMemoryLotRepository();
        CurrentUserService utilisateurs = new CurrentUserService();
        agent = utilisateurs.resoudre("agent-1");
        service = new StockService(lots, (type, qualite) -> BigDecimal.valueOf(800), utilisateurs);
    }

    @Test
    void calcule_total_reserve_et_disponible() {
        lots.enregistrer(lot(1000, LocalDate.of(2026, 9, 1)));
        lots.enregistrer(lot(500, LocalDate.of(2026, 9, 2)));

        List<StockLigne> lignes = service.consulter(agent, null, null);

        assertThat(lignes).hasSize(1);
        StockLigne ligne = lignes.getFirst();
        assertThat(ligne.totalKg()).isEqualByComparingTo("1500");
        assertThat(ligne.reserveKg()).isEqualByComparingTo("800");
        assertThat(ligne.disponibleKg()).isEqualByComparingTo("700");
    }

    @Test
    void consomme_les_lots_du_plus_ancien_au_plus_recent() {
        LocalDate ancienne = LocalDate.of(2026, 9, 1);
        LocalDate recente = LocalDate.of(2026, 10, 1);
        Lot recent = lot(1000, recente);
        Lot ancien = lot(500, ancienne);
        lots.enregistrer(recent);
        lots.enregistrer(ancien);

        List<LotConsomme> consommes = service.consommer(TypeCulture.ARACHIDE, Qualite.A,
                BigDecimal.valueOf(700));

        assertThat(consommes).hasSize(2);
        assertThat(consommes.get(0).lotId()).isEqualTo(ancien.id());
        assertThat(consommes.get(0).quantiteKg()).isEqualByComparingTo("500");
        assertThat(consommes.get(1).lotId()).isEqualTo(recent.id());
        assertThat(consommes.get(1).quantiteKg()).isEqualByComparingTo("200");
        assertThat(ancien.estEpuise()).isTrue();
        assertThat(recent.quantiteDisponible()).isEqualByComparingTo("800");
    }

    @Test
    void refuse_une_consommation_superieure_au_stock_sans_le_modifier() {
        Lot lot = lot(1500, LocalDate.of(2026, 9, 1));
        lots.enregistrer(lot);

        assertThatThrownBy(() -> service.consommer(TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(2000)))
                .isInstanceOf(RegleMetierException.class);

        assertThat(lot.quantiteDisponible()).isEqualByComparingTo("1500");
    }

    private Lot lot(long quantite, LocalDate date) {
        return Lot.creer(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(quantite), date);
    }
}
