package sn.sunagri.production.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.port.out.ProducteurRepository;
import sn.sunagri.production.domain.Parcelle;
import sn.sunagri.production.domain.Producteur;
import sn.sunagri.shared.config.ParametresProperties;
import sn.sunagri.shared.domain.ConflitException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;

@Service
public class ProducteurService {

    private final ProducteurRepository producteurs;
    private final CurrentUserService utilisateurs;
    private final ParametresProperties parametres;

    public ProducteurService(ProducteurRepository producteurs, CurrentUserService utilisateurs,
            ParametresProperties parametres) {
        this.producteurs = producteurs;
        this.utilisateurs = utilisateurs;
        this.parametres = parametres;
    }

    public Producteur enregistrer(Utilisateur utilisateur, String nom, String prenom, String telephone, String village) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        String telephoneNormalise = normaliserTelephone(telephone);
        if (producteurs.existeTelephone(telephoneNormalise)) {
            throw new ConflitException("TELEPHONE_DEJA_UTILISE",
                    "Un producteur utilise déjà le téléphone " + telephoneNormalise + ".");
        }
        Producteur producteur = Producteur.creer(UUID.randomUUID(), nom, prenom, telephoneNormalise, village);
        return producteurs.enregistrer(producteur);
    }

    public Parcelle declarerParcelle(Utilisateur utilisateur, UUID producteurId, String nom, BigDecimal superficie,
            String commune) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Producteur producteur = charger(producteurId);
        Parcelle parcelle = producteur.ajouterParcelle(UUID.randomUUID(), nom, superficie, commune,
                parametres.getSuperficieMaxProducteurHa());
        producteurs.enregistrer(producteur);
        return parcelle;
    }

    public List<Producteur> tous(Utilisateur utilisateur) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        return producteurs.tous();
    }

    public Producteur detail(Utilisateur utilisateur, UUID producteurId) {
        utilisateurs.verifierAccesProducteur(utilisateur, producteurId.toString());
        return charger(producteurId);
    }

    public List<Parcelle> parcelles(Utilisateur utilisateur, UUID producteurId) {
        utilisateurs.verifierAccesProducteur(utilisateur, producteurId.toString());
        return charger(producteurId).parcelles();
    }

    public Parcelle parcelle(Utilisateur utilisateur, UUID producteurId, UUID parcelleId) {
        utilisateurs.verifierAccesProducteur(utilisateur, producteurId.toString());
        return charger(producteurId).parcelles().stream()
                .filter(parcelle -> parcelle.id().equals(parcelleId))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Parcelle introuvable."));
    }

    private Producteur charger(UUID producteurId) {
        return producteurs.parId(producteurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Producteur introuvable."));
    }

    private String normaliserTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("\\s+", "").trim();
    }
}
