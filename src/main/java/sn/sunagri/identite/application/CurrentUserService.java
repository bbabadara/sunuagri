package sn.sunagri.identite.application;

import org.springframework.stereotype.Service;
import sn.sunagri.identite.domain.Role;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.AccesRefuseException;
import sn.sunagri.shared.domain.AuthentificationException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;

/**
 * Identité simulée : l'en-tête X-User-Id détermine l'utilisateur courant.
 * Aucune authentification réelle (hors périmètre du sujet).
 */
@Service
public class CurrentUserService {

    private static final String PREFIXE_ACHETEUR = "acheteur:";
    private static final String PREFIXE_PRODUCTEUR = "producteur:";

    public Utilisateur resoudre(String entete) {
        if (entete == null || entete.isBlank()) {
            throw new AuthentificationException();
        }
        String valeur = entete.trim();
        if ("agent-1".equals(valeur)) {
            return new Utilisateur("agent-1", Role.AGENT, null, null);
        }
        if ("admin-1".equals(valeur)) {
            return new Utilisateur("admin-1", Role.ADMIN, null, null);
        }
        if (valeur.startsWith(PREFIXE_ACHETEUR)) {
            String identifiant = valeur.substring(PREFIXE_ACHETEUR.length()).trim();
            if (identifiant.isEmpty()) {
                throw new AuthentificationException();
            }
            return new Utilisateur(valeur, Role.ACHETEUR, null, identifiant);
        }
        if (valeur.startsWith(PREFIXE_PRODUCTEUR)) {
            String identifiant = valeur.substring(PREFIXE_PRODUCTEUR.length()).trim();
            if (identifiant.isEmpty()) {
                throw new AuthentificationException();
            }
            return new Utilisateur(valeur, Role.PRODUCTEUR, identifiant, null);
        }
        throw new AuthentificationException();
    }

    public Utilisateur exigerRole(Utilisateur utilisateur, Role... roles) {
        for (Role role : roles) {
            if (utilisateur.role() == role) {
                return utilisateur;
            }
        }
        throw new AccesRefuseException("Votre rôle ne permet pas cette action.");
    }

    public Utilisateur exigerAgentOuAdmin(Utilisateur utilisateur) {
        return exigerRole(utilisateur, Role.AGENT, Role.ADMIN);
    }

    /** Accès à un producteur : un producteur ne voit que lui-même, sinon 404 pour ne rien révéler. */
    public void verifierAccesProducteur(Utilisateur utilisateur, String producteurId) {
        if (utilisateur.estAgentOuAdmin()) {
            return;
        }
        if (utilisateur.role() == Role.PRODUCTEUR && producteurId.equals(utilisateur.producteurId())) {
            return;
        }
        throw new RessourceIntrouvableException("Producteur introuvable.");
    }

    /** Accès à une commande : un acheteur ne voit que les siennes, sinon 404. */
    public void verifierAccesAcheteur(Utilisateur utilisateur, String acheteurId) {
        if (utilisateur.estAgentOuAdmin()) {
            return;
        }
        if (utilisateur.role() == Role.ACHETEUR && acheteurId.equals(utilisateur.acheteurId())) {
            return;
        }
        throw new RessourceIntrouvableException("Commande introuvable.");
    }
}
