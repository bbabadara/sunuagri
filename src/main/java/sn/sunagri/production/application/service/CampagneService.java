package sn.sunagri.production.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.port.out.CampagneRepository;
import sn.sunagri.production.domain.Campagne;
import sn.sunagri.production.domain.Saison;
import sn.sunagri.shared.domain.ConflitException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;

@Service
public class CampagneService {

    private final CampagneRepository campagnes;
    private final CurrentUserService utilisateurs;

    public CampagneService(CampagneRepository campagnes, CurrentUserService utilisateurs) {
        this.campagnes = campagnes;
        this.utilisateurs = utilisateurs;
    }

    public Campagne ouvrir(Utilisateur utilisateur, Saison saison, int annee, LocalDate dateDebut, LocalDate dateFin) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        if (campagnes.existe(saison, annee)) {
            throw new ConflitException("CAMPAGNE_DEJA_OUVERTE",
                    "Une campagne " + saison + " existe déjà pour l'année " + annee + ".");
        }
        Campagne campagne = Campagne.ouvrir(UUID.randomUUID(), saison, annee, dateDebut, dateFin);
        return campagnes.enregistrer(campagne);
    }

    public Campagne cloturer(Utilisateur utilisateur, UUID campagneId) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Campagne campagne = parId(campagneId);
        campagne.cloturer();
        return campagnes.enregistrer(campagne);
    }

    public List<Campagne> toutes(Utilisateur utilisateur) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        return campagnes.toutes();
    }

    public Campagne parId(UUID campagneId) {
        return campagnes.parId(campagneId)
                .orElseThrow(() -> new RessourceIntrouvableException("Campagne introuvable."));
    }
}
