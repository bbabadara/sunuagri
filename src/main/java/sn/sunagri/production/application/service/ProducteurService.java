package sn.sunagri.production.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.RapportImportProducteurs;
import sn.sunagri.production.application.port.out.ProducteurRepository;
import sn.sunagri.production.domain.Parcelle;
import sn.sunagri.production.domain.Producteur;
import sn.sunagri.shared.config.ParametresProperties;
import sn.sunagri.shared.domain.ConflitException;
import sn.sunagri.shared.domain.DomaineException;
import sn.sunagri.shared.domain.RegleMetierException;
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

    public RapportImportProducteurs importer(Utilisateur utilisateur, String contenuCsv) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        if (contenuCsv == null || contenuCsv.isBlank()) {
            throw new RegleMetierException("FICHIER_VIDE", "Le fichier CSV est vide.");
        }
        String contenu = contenuCsv.startsWith("\uFEFF") ? contenuCsv.substring(1) : contenuCsv;
        String[] lignes = contenu.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        List<RapportImportProducteurs.ProducteurImporte> acceptes = new ArrayList<>();
        List<RapportImportProducteurs.LigneRejetee> rejets = new ArrayList<>();
        int total = 0;
        boolean premiereLigne = true;
        for (int index = 0; index < lignes.length; index++) {
            int numero = index + 1;
            String ligne = lignes[index].strip();
            if (ligne.isEmpty() || ligne.startsWith("#")) {
                continue;
            }
            String[] cellules = ligne.split(";", -1);
            if (premiereLigne) {
                premiereLigne = false;
                if (estEntete(cellules)) {
                    continue;
                }
            }
            total++;
            if (cellules.length != 4) {
                rejets.add(new RapportImportProducteurs.LigneRejetee(numero,
                        "Format attendu : nom;prénom;téléphone;village."));
                continue;
            }
            try {
                Producteur producteur = enregistrer(utilisateur, cellules[0], cellules[1], cellules[2], cellules[3]);
                acceptes.add(new RapportImportProducteurs.ProducteurImporte(numero, producteur.id(),
                        producteur.nom(), producteur.prenom(), producteur.telephone(), producteur.village()));
            } catch (DomaineException | IllegalArgumentException refus) {
                rejets.add(new RapportImportProducteurs.LigneRejetee(numero, refus.getMessage()));
            }
        }
        return new RapportImportProducteurs(total, acceptes.size(), rejets.size(),
                List.copyOf(acceptes), List.copyOf(rejets));
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

    private boolean estEntete(String[] cellules) {
        return cellules.length > 0 && "nom".equalsIgnoreCase(cellules[0].strip())
                && cellules.length > 1 && normaliserEntete(cellules[1]).equals("prenom");
    }

    private String normaliserEntete(String valeur) {
        return valeur.strip().toLowerCase()
                .replace("é", "e")
                .replace("è", "e");
    }

    private String normaliserTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        return telephone.replaceAll("\\s+", "").trim();
    }
}
