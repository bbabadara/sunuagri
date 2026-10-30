package sn.sunagri.production.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.adapter.out.memory.InMemoryProducteurRepository;
import sn.sunagri.production.application.service.ProducteurService;
import sn.sunagri.production.domain.Producteur;
import sn.sunagri.shared.config.ParametresProperties;
import sn.sunagri.shared.domain.AccesRefuseException;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;

class ProducteurServiceTest {

    private InMemoryProducteurRepository producteurs;
    private ProducteurService service;
    private CurrentUserService utilisateurs;
    private Utilisateur agent;

    @BeforeEach
    void setUp() {
        producteurs = new InMemoryProducteurRepository();
        utilisateurs = new CurrentUserService();
        agent = utilisateurs.resoudre("agent-1");
        service = new ProducteurService(producteurs, utilisateurs, new ParametresProperties());
    }

    @Test
    void importe_les_lignes_valides_et_rapporte_les_lignes_refusees() {
        String csv = """
                nom;prénom;téléphone;village
                Diop;Awa;77 000 00 01;Thiès
                Fall;Moussa;770000002;Kaolack
                Invalide;Trois;colonnes
                ;Sans;770000004;Nom
                Doublon;Awa;770000001;Dakar
                """;

        RapportImportProducteurs rapport = service.importer(agent, csv);

        assertThat(rapport.totalLignes()).isEqualTo(5);
        assertThat(rapport.acceptees()).isEqualTo(2);
        assertThat(rapport.refusees()).isEqualTo(3);
        assertThat(rapport.rejets()).extracting(RapportImportProducteurs.LigneRejetee::ligne)
                .containsExactly(4, 5, 6);
        assertThat(producteurs.tous()).hasSize(2);
        assertThat(producteurs.existeTelephone("770000001")).isTrue();
    }

    @Test
    void accepte_un_fichier_sans_entete() {
        RapportImportProducteurs rapport = service.importer(agent, "Diop;Awa;770000001;Thiès");

        assertThat(rapport.totalLignes()).isEqualTo(1);
        assertThat(rapport.acceptees()).isEqualTo(1);
        assertThat(rapport.refusees()).isZero();
    }

    @Test
    void un_fichier_vide_est_refuse() {
        assertThatThrownBy(() -> service.importer(agent, "   "))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("vide");
    }

    @Test
    void un_non_agent_ne_peut_pas_importer() {
        Utilisateur acheteur = utilisateurs.resoudre("acheteur:client-1");

        assertThatThrownBy(() -> service.importer(acheteur, "Diop;Awa;770000001;Thiès"))
                .isInstanceOf(AccesRefuseException.class);
        assertThat(producteurs.tous()).isEmpty();
    }

    @Test
    void un_producteur_ne_voit_que_ses_propres_donnees() {
        Producteur producteur = service.enregistrer(agent, "Diop", "Awa", "770000001", "Thiès");
        service.enregistrer(agent, "Fall", "Moussa", "770000002", "Kaolack");
        service.declarerParcelle(agent, producteur.id(), "Champ nord", BigDecimal.TEN, "Thiès");
        Utilisateur proprietaire = utilisateurs.resoudre("producteur:" + producteur.id());
        Utilisateur autre = utilisateurs.resoudre("producteur:00000000-0000-0000-0000-000000000099");

        assertThat(service.detail(proprietaire, producteur.id()).nom()).isEqualTo("Diop");
        assertThat(service.parcelles(proprietaire, producteur.id())).hasSize(1);
        assertThatThrownBy(() -> service.detail(autre, producteur.id()))
                .isInstanceOf(RessourceIntrouvableException.class);
        assertThatThrownBy(() -> service.parcelles(autre, producteur.id()))
                .isInstanceOf(RessourceIntrouvableException.class);
    }
}
