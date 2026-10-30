package sn.sunagri.commercialisation.application.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.commercialisation.application.port.out.CommandeRepository;
import sn.sunagri.commercialisation.application.port.out.PaiementRepository;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.PaiementDu;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.RessourceIntrouvableException;

@Service
public class PaiementService {

    private final PaiementRepository paiements;
    private final CommandeRepository commandes;
    private final CurrentUserService utilisateurs;

    public PaiementService(PaiementRepository paiements, CommandeRepository commandes,
            CurrentUserService utilisateurs) {
        this.paiements = paiements;
        this.commandes = commandes;
        this.utilisateurs = utilisateurs;
    }

    public List<PaiementDu> tous(Utilisateur utilisateur) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        return paiements.tous();
    }

    public List<PaiementDu> parCommande(Utilisateur utilisateur, UUID commandeId) {
        Commande commande = commandes.parId(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Commande introuvable."));
        utilisateurs.verifierAccesAcheteur(utilisateur, commande.acheteurId());
        return paiements.parCommande(commandeId);
    }

    public List<PaiementDu> parProducteur(Utilisateur utilisateur, UUID producteurId) {
        utilisateurs.verifierAccesProducteur(utilisateur, producteurId.toString());
        return paiements.parProducteur(producteurId);
    }

    public PaiementDu marquerPaye(Utilisateur utilisateur, UUID paiementId) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        PaiementDu paiement = paiements.parId(paiementId)
                .orElseThrow(() -> new RessourceIntrouvableException("Paiement introuvable."));
        paiement.marquerPaye();
        return paiements.enregistrer(paiement);
    }
}
