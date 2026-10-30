package sn.sunagri.commercialisation.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sn.sunagri.commercialisation.application.ResultatLivraison;
import sn.sunagri.commercialisation.application.port.out.CommandeRepository;
import sn.sunagri.commercialisation.application.port.out.NotificationPaiementPort;
import sn.sunagri.commercialisation.application.port.out.OffreRepository;
import sn.sunagri.commercialisation.application.port.out.PaiementRepository;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.Offre;
import sn.sunagri.commercialisation.domain.PaiementDu;
import sn.sunagri.commercialisation.domain.RepartitionPaiement;
import sn.sunagri.commercialisation.domain.ResultatRepartition;
import sn.sunagri.commercialisation.domain.StatutCommande;
import sn.sunagri.entrepot.application.StockPort;
import sn.sunagri.entrepot.domain.LotConsomme;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Role;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.ContactProducteur;
import sn.sunagri.production.application.ProducteurDirectoryPort;
import sn.sunagri.shared.api.PageResponse;
import sn.sunagri.shared.config.ParametresProperties;
import sn.sunagri.shared.domain.ClockPort;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;
import sn.sunagri.shared.domain.Validations;

@Service
public class CommandeService {

    private static final Logger log = LoggerFactory.getLogger(CommandeService.class);

    private final CommandeRepository commandes;
    private final OffreRepository offres;
    private final PaiementRepository paiements;
    private final StockPort stock;
    private final ProducteurDirectoryPort producteurs;
    private final NotificationPaiementPort notifications;
    private final ParametresProperties parametres;
    private final CurrentUserService utilisateurs;
    private final ClockPort horloge;

    public CommandeService(CommandeRepository commandes, OffreRepository offres, PaiementRepository paiements,
            StockPort stock, ProducteurDirectoryPort producteurs, NotificationPaiementPort notifications,
            ParametresProperties parametres, CurrentUserService utilisateurs, ClockPort horloge) {
        this.commandes = commandes;
        this.offres = offres;
        this.paiements = paiements;
        this.stock = stock;
        this.producteurs = producteurs;
        this.notifications = notifications;
        this.parametres = parametres;
        this.utilisateurs = utilisateurs;
        this.horloge = horloge;
    }

    public Commande passer(Utilisateur utilisateur, UUID offreId, BigDecimal quantiteKg) {
        utilisateurs.exigerRole(utilisateur, Role.ACHETEUR);
        Validations.strictementPositif(quantiteKg, "quantité commandée");
        BigDecimal minimale = parametres.getCommandeMinimaleKg();
        if (quantiteKg.compareTo(minimale) < 0) {
            throw new RegleMetierException("COMMANDE_MINIMALE_NON_ATTEINTE",
                    "La quantité minimale d'une commande est de " + minimale.toPlainString() + " kg.");
        }
        Offre offre = chargerOffre(offreId);
        offre.commander(quantiteKg);
        Commande commande = Commande.creer(UUID.randomUUID(), offre.id(), utilisateur.acheteurId(),
                offre.typeCulture(), offre.qualite(), quantiteKg, offre.prixKg(), horloge.aujourdHui());
        offres.enregistrer(offre);
        return commandes.enregistrer(commande);
    }

    public Commande annuler(Utilisateur utilisateur, UUID commandeId) {
        Commande commande = chargerCommande(commandeId);
        utilisateurs.verifierAccesAcheteur(utilisateur, commande.acheteurId());
        commande.annuler();
        Offre offre = chargerOffre(commande.offreId());
        offre.annulerCommande(commande.quantiteKg());
        offres.enregistrer(offre);
        return commandes.enregistrer(commande);
    }

    public PageResponse<Commande> historique(Utilisateur utilisateur, StatutCommande statut, int page, int size) {
        utilisateurs.exigerRole(utilisateur, Role.ACHETEUR, Role.AGENT, Role.ADMIN);
        int pageSure = Math.max(page, 0);
        int tailleSure = Math.min(Math.max(size, 1), 100);
        String acheteurId = utilisateur.role() == Role.ACHETEUR ? utilisateur.acheteurId() : null;
        List<Commande> contenu = commandes.rechercher(acheteurId, statut, pageSure, tailleSure);
        long total = commandes.compter(acheteurId, statut);
        return PageResponse.de(contenu, pageSure, tailleSure, total);
    }

    public Commande detail(Utilisateur utilisateur, UUID commandeId) {
        Commande commande = chargerCommande(commandeId);
        utilisateurs.verifierAccesAcheteur(utilisateur, commande.acheteurId());
        return commande;
    }

    public ResultatLivraison livrer(Utilisateur utilisateur, UUID commandeId) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Commande commande = chargerCommande(commandeId);
        if (!commande.estEnAttente()) {
            throw new RegleMetierException("COMMANDE_NON_LIVRABLE",
                    "Une commande déjà livrée ou annulée ne peut pas être livrée.");
        }
        Offre offre = chargerOffre(commande.offreId());
        List<LotConsomme> consommes = stock.consommer(commande.typeCulture(), commande.qualite(),
                commande.quantiteKg());
        Map<UUID, BigDecimal> quantitesParProducteur = new LinkedHashMap<>();
        for (LotConsomme consomme : consommes) {
            quantitesParProducteur.merge(consomme.producteurId(), consomme.quantiteKg(), BigDecimal::add);
        }
        ResultatRepartition repartition = RepartitionPaiement.calculer(commande.montantXof(),
                parametres.getTauxCommission(), quantitesParProducteur);

        List<PaiementDu> paiementsCrees = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> part : repartition.montantsParProducteur().entrySet()) {
            if (part.getValue().signum() <= 0) {
                continue;
            }
            PaiementDu paiement = PaiementDu.creer(UUID.randomUUID(), commande.id(), part.getKey(),
                    part.getValue(), horloge.aujourdHui());
            paiementsCrees.add(paiements.enregistrer(paiement));
        }

        offre.enregistrerLivraison(commande.quantiteKg());
        offres.enregistrer(offre);
        commande.livrer();
        commandes.enregistrer(commande);
        notifier(paiementsCrees);
        return new ResultatLivraison(commande, repartition.commissionXof(), List.copyOf(paiementsCrees));
    }

    private void notifier(List<PaiementDu> paiementsCrees) {
        for (PaiementDu paiement : paiementsCrees) {
            ContactProducteur contact = producteurs.contact(paiement.producteurId()).orElse(null);
            boolean envoye = notifications.envoyer(paiement, contact);
            if (envoye) {
                paiement.marquerSmsEnvoye();
                paiements.enregistrer(paiement);
            } else {
                log.warn("Notification SMS non envoyée pour le paiement {}", paiement.id());
            }
        }
    }

    private Commande chargerCommande(UUID commandeId) {
        return commandes.parId(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Commande introuvable."));
    }

    private Offre chargerOffre(UUID offreId) {
        return offres.parId(offreId)
                .orElseThrow(() -> new RessourceIntrouvableException("Offre introuvable."));
    }
}
