package sn.sunagri.entrepot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.sunagri.entrepot.adapter.out.memory.InMemoryLivraisonRepository;
import sn.sunagri.entrepot.adapter.out.memory.InMemoryLotRepository;
import sn.sunagri.entrepot.application.service.LivraisonService;
import sn.sunagri.entrepot.domain.Livraison;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.adapter.out.memory.InMemoryCultureRepository;
import sn.sunagri.production.domain.Culture;
import sn.sunagri.production.domain.ParametresCulture;
import sn.sunagri.shared.domain.ClockPort;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;

class LivraisonServiceTest {

    private InMemoryCultureRepository cultures;
    private InMemoryLotRepository lots;
    private LivraisonService service;
    private Utilisateur agent;
    private Culture culture;

    @BeforeEach
    void setUp() {
        cultures = new InMemoryCultureRepository();
        lots = new InMemoryLotRepository();
        ClockPort horloge = () -> LocalDate.of(2026, 10, 1);
        CurrentUserService utilisateurs = new CurrentUserService();
        agent = utilisateurs.resoudre("agent-1");
        service = new LivraisonService(cultures, new InMemoryLivraisonRepository(), lots, horloge, utilisateurs);
        culture = cultureRecoltee();
        cultures.enregistrer(culture);
    }

    @Test
    void une_livraison_acceptee_cree_un_lot() {
        Livraison livraison = service.livrer(agent, culture.id(), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(8), Qualite.A);

        assertThat(livraison.lotId()).isNotNull();
        assertThat(lots.parId(livraison.lotId())).isPresent()
                .get()
                .satisfies(lot -> {
                    assertThat(lot.quantiteDisponible()).isEqualByComparingTo("1000");
                    assertThat(lot.producteurId()).isEqualTo(culture.producteurId());
                    assertThat(lot.qualite()).isEqualTo(Qualite.A);
                });
    }

    @Test
    void une_humidite_trop_elevee_est_refusee_sans_modifier_le_stock() {
        assertThatThrownBy(() -> service.livrer(agent, culture.id(), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10), Qualite.A))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("humidité");

        assertThat(lots.tous()).isEmpty();
    }

    @Test
    void le_cumul_des_livraisons_ne_depasse_pas_la_recolte() {
        service.livrer(agent, culture.id(), BigDecimal.valueOf(1500), BigDecimal.valueOf(8), Qualite.A);

        assertThatThrownBy(() -> service.livrer(agent, culture.id(), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(8), Qualite.A))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("récoltée");

        assertThat(lots.tous()).hasSize(1);
    }

    @Test
    void une_culture_non_recoltee_ne_peut_pas_etre_livree() {
        Culture planifiee = Culture.planifier(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), TypeCulture.MIL, BigDecimal.ONE,
                new ParametresCulture(BigDecimal.valueOf(800), BigDecimal.valueOf(12), 75));
        cultures.enregistrer(planifiee);

        assertThatThrownBy(() -> service.livrer(agent, planifiee.id(), BigDecimal.TEN,
                BigDecimal.ONE, Qualite.B))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("récoltée");
    }

    private Culture cultureRecoltee() {
        Culture culture = Culture.planifier(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), TypeCulture.ARACHIDE, BigDecimal.valueOf(2),
                new ParametresCulture(BigDecimal.valueOf(1200), BigDecimal.valueOf(9), 90));
        culture.semer(LocalDate.of(2026, 6, 1));
        culture.recolter(LocalDate.of(2026, 9, 1), BigDecimal.valueOf(2400));
        return culture;
    }
}
