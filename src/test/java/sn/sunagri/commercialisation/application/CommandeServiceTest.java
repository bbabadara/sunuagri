package sn.sunagri.commercialisation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.sunagri.commercialisation.adapter.out.memory.InMemoryCommandeRepository;
import sn.sunagri.commercialisation.adapter.out.memory.InMemoryOffreRepository;
import sn.sunagri.commercialisation.adapter.out.memory.InMemoryPaiementRepository;
import sn.sunagri.commercialisation.adapter.out.stock.ReservationsStockAdapter;
import sn.sunagri.commercialisation.application.service.CommandeService;
import sn.sunagri.commercialisation.application.service.OffreService;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.Offre;
import sn.sunagri.commercialisation.domain.PaiementDu;
import sn.sunagri.commercialisation.domain.StatutCommande;
import sn.sunagri.commercialisation.domain.StatutOffre;
import sn.sunagri.commercialisation.domain.StatutPaiement;
import sn.sunagri.entrepot.adapter.out.memory.InMemoryLotRepository;
import sn.sunagri.entrepot.application.service.StockService;
import sn.sunagri.entrepot.domain.Lot;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.notification.adapter.out.paiement.NotificationPaiementSimulee;
import sn.sunagri.notification.application.JournalNotifications;
import sn.sunagri.notification.application.port.out.SmsSender;
import sn.sunagri.production.adapter.out.directory.ProducteurDirectoryAdapter;
import sn.sunagri.production.adapter.out.memory.InMemoryProducteurRepository;
import sn.sunagri.production.domain.Producteur;
import sn.sunagri.shared.config.ParametresProperties;
import sn.sunagri.shared.domain.ClockPort;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;

class CommandeServiceTest {

    private static final LocalDate AUJOURD_HUI = LocalDate.of(2026, 10, 15);

    private InMemoryLotRepository lots;
    private InMemoryOffreRepository offres;
    private InMemoryCommandeRepository commandes;
    private InMemoryPaiementRepository paiements;
    private InMemoryProducteurRepository producteurs;
    private JournalNotifications journal;
    private StockService stock;
    private OffreService offreService;
    private CommandeService commandeService;
    private Utilisateur agent;
    private Utilisateur acheteur;
    private Producteur producteurAncien;
    private Producteur producteurRecent;

    @BeforeEach
    void setUp() {
        lots = new InMemoryLotRepository();
        offres = new InMemoryOffreRepository();
        commandes = new InMemoryCommandeRepository();
        paiements = new InMemoryPaiementRepository();
        producteurs = new InMemoryProducteurRepository();
        journal = new JournalNotifications();
        ClockPort horloge = () -> AUJOURD_HUI;
        CurrentUserService utilisateurs = new CurrentUserService();
        agent = utilisateurs.resoudre("agent-1");
        acheteur = utilisateurs.resoudre("acheteur:client-1");

        producteurAncien = Producteur.creer(UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Diop", "Awa", "770000001", "Thiès");
        producteurRecent = Producteur.creer(UUID.fromString("00000000-0000-0000-0000-000000000002"),
                "Fall", "Moussa", "770000002", "Kaolack");
        producteurs.enregistrer(producteurAncien);
        producteurs.enregistrer(producteurRecent);

        lots.enregistrer(Lot.creer(UUID.randomUUID(), UUID.randomUUID(), producteurAncien.id(),
                TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(600), AUJOURD_HUI.minusDays(10)));
        lots.enregistrer(Lot.creer(UUID.randomUUID(), UUID.randomUUID(), producteurRecent.id(),
                TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(400), AUJOURD_HUI.minusDays(2)));

        stock = new StockService(lots, new ReservationsStockAdapter(offres), utilisateurs);
        ProducteurDirectoryAdapter directory = new ProducteurDirectoryAdapter(producteurs);
        NotificationPaiementSimulee notifications = new NotificationPaiementSimulee(
                smsQuiReussit(smsEnvoyes()), journal, horloge);
        ParametresProperties parametres = new ParametresProperties();
        offreService = new OffreService(offres, stock, utilisateurs, horloge);
        commandeService = new CommandeService(commandes, offres, paiements, stock, directory, notifications,
                parametres, utilisateurs, horloge);
    }

    @Test
    void la_livraison_consomme_les_lots_fifo_et_repartit_le_paiement() {
        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300));
        Commande commande = commandeService.passer(acheteur, offre.id(), BigDecimal.valueOf(1000));

        ResultatLivraison resultat = commandeService.livrer(agent, commande.id());

        assertThat(resultat.commande().statut()).isEqualTo(StatutCommande.LIVREE);
        assertThat(resultat.commissionXof()).isEqualByComparingTo("15000");
        assertThat(resultat.paiements()).hasSize(2);

        PaiementDu paiementAncien = paiementDe(resultat.paiements(), producteurAncien.id());
        PaiementDu paiementRecent = paiementDe(resultat.paiements(), producteurRecent.id());
        assertThat(paiementAncien.montantXof()).isEqualByComparingTo("171000");
        assertThat(paiementRecent.montantXof()).isEqualByComparingTo("114000");
        assertThat(paiementAncien.montantXof().add(paiementRecent.montantXof())
                .add(resultat.commissionXof())).isEqualByComparingTo("300000");
        assertThat(paiementAncien.statut()).isEqualTo(StatutPaiement.A_PAYER);
        assertThat(paiementAncien.smsEnvoye()).isTrue();
        assertThat(paiementRecent.smsEnvoye()).isTrue();
        assertThat(journal.entrees()).hasSize(2).allMatch(entree -> entree.succes());

        Offre offreApres = offres.parId(offre.id()).orElseThrow();
        assertThat(offreApres.quantiteLivree()).isEqualByComparingTo("1000");
        assertThat(offreApres.statut()).isEqualTo(StatutOffre.EPUISEE);
        assertThat(lots.tous()).allMatch(Lot::estEpuise);
    }

    @Test
    void une_commande_ne_peut_etre_livree_qu_une_seule_fois() {
        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300));
        Commande commande = commandeService.passer(acheteur, offre.id(), BigDecimal.valueOf(1000));
        commandeService.livrer(agent, commande.id());

        assertThatThrownBy(() -> commandeService.livrer(agent, commande.id()))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("déjà livrée");
        assertThat(paiements.tous()).hasSize(2);
    }

    @Test
    void un_echec_sms_ne_bloque_pas_la_livraison_et_est_trace() {
        StockService stock = new StockService(lots, new ReservationsStockAdapter(offres),
                new CurrentUserService());
        ProducteurDirectoryAdapter directory = new ProducteurDirectoryAdapter(producteurs);
        SmsSender smsDefaillant = (telephone, message) -> {
            if ("770000002".equals(telephone)) {
                throw new IllegalStateException("Passerelle indisponible");
            }
        };
        ClockPort horloge = () -> AUJOURD_HUI;
        NotificationPaiementSimulee notifications = new NotificationPaiementSimulee(smsDefaillant, journal, horloge);
        CommandeService service = new CommandeService(commandes, offres, paiements, stock, directory, notifications,
                new ParametresProperties(), new CurrentUserService(), horloge);

        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300));
        Commande commande = service.passer(acheteur, offre.id(), BigDecimal.valueOf(1000));

        ResultatLivraison resultat = service.livrer(agent, commande.id());

        assertThat(resultat.commande().statut()).isEqualTo(StatutCommande.LIVREE);
        assertThat(paiementDe(resultat.paiements(), producteurAncien.id()).smsEnvoye()).isTrue();
        assertThat(paiementDe(resultat.paiements(), producteurRecent.id()).smsEnvoye()).isFalse();
        assertThat(journal.entrees()).anyMatch(entree -> !entree.succes());
    }

    @Test
    void annuler_une_commande_rend_la_quantite_a_l_offre() {
        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300));
        Commande commande = commandeService.passer(acheteur, offre.id(), BigDecimal.valueOf(600));

        commandeService.annuler(acheteur, commande.id());

        Offre offreApres = offres.parId(offre.id()).orElseThrow();
        assertThat(offreApres.quantiteRestante()).isEqualByComparingTo("1000");
        assertThat(offreApres.statut()).isEqualTo(StatutOffre.OUVERTE);
        assertThat(commandes.parId(commande.id()).orElseThrow().statut())
                .isEqualTo(StatutCommande.ANNULEE);
    }

    @Test
    void un_acheteur_ne_voit_pas_la_commande_d_un_autre() {
        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300));
        Commande commande = commandeService.passer(acheteur, offre.id(), BigDecimal.valueOf(100));
        Utilisateur autre = new CurrentUserService().resoudre("acheteur:client-2");

        assertThatThrownBy(() -> commandeService.detail(autre, commande.id()))
                .hasMessageContaining("introuvable");
    }

    @Test
    void une_commande_sous_le_minimum_est_refusee() {
        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(300));

        assertThatThrownBy(() -> commandeService.passer(acheteur, offre.id(), BigDecimal.valueOf(49)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("50");
        assertThat(commandes.toutes()).isEmpty();
    }

    @Test
    void publier_une_offre_reserve_le_stock_et_le_retrait_le_libere() {
        Offre offre = offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A, BigDecimal.valueOf(600),
                BigDecimal.valueOf(300));

        assertThat(stock.quantiteDisponible(TypeCulture.ARACHIDE, Qualite.A)).isEqualByComparingTo("400");

        offreService.retirer(agent, offre.id());

        assertThat(stock.quantiteDisponible(TypeCulture.ARACHIDE, Qualite.A)).isEqualByComparingTo("1000");
    }

    @Test
    void une_offre_ne_peut_pas_depasser_le_stock_disponible() {
        assertThatThrownBy(() -> offreService.publier(agent, TypeCulture.ARACHIDE, Qualite.A,
                BigDecimal.valueOf(1001), BigDecimal.valueOf(300)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("insuffisant");
        assertThat(offres.toutes()).isEmpty();
    }

    private List<String> smsEnvoyes() {
        return new ArrayList<>();
    }

    private SmsSender smsQuiReussit(List<String> messages) {
        return (telephone, message) -> messages.add(telephone + "|" + message);
    }

    private PaiementDu paiementDe(List<PaiementDu> paiementsDus, UUID producteurId) {
        return paiementsDus.stream()
                .filter(paiement -> paiement.producteurId().equals(producteurId))
                .findFirst()
                .orElseThrow();
    }
}
