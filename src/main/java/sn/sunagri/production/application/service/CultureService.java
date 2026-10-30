package sn.sunagri.production.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.port.out.CampagneRepository;
import sn.sunagri.production.application.port.out.CultureRepository;
import sn.sunagri.production.application.port.out.ProducteurRepository;
import sn.sunagri.production.domain.Campagne;
import sn.sunagri.production.domain.Culture;
import sn.sunagri.production.domain.ParametresCulture;
import sn.sunagri.production.domain.Parcelle;
import sn.sunagri.production.domain.Producteur;
import sn.sunagri.referentiel.application.port.out.CultureReferentielRepository;
import sn.sunagri.referentiel.domain.CultureReferentiel;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

@Service
public class CultureService {

    private final CultureRepository cultures;
    private final CampagneRepository campagnes;
    private final ProducteurRepository producteurs;
    private final CultureReferentielRepository referentiels;
    private final CurrentUserService utilisateurs;

    public CultureService(CultureRepository cultures, CampagneRepository campagnes,
            ProducteurRepository producteurs, CultureReferentielRepository referentiels,
            CurrentUserService utilisateurs) {
        this.cultures = cultures;
        this.campagnes = campagnes;
        this.producteurs = producteurs;
        this.referentiels = referentiels;
        this.utilisateurs = utilisateurs;
    }

    public Culture declarer(Utilisateur utilisateur, UUID parcelleId, UUID campagneId, TypeCulture type,
            BigDecimal superficieEmblavee) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Validations.strictementPositif(superficieEmblavee, "superficie emblavée");

        Campagne campagne = campagnes.parId(campagneId)
                .orElseThrow(() -> new RessourceIntrouvableException("Campagne introuvable."));
        campagne.exigerOuverte();

        CultureReferentiel referentiel = referentiels.parType(type)
                .orElseThrow(() -> new RegleMetierException("CULTURE_INCONNUE",
                        "La culture « " + type + " » ne fait pas partie de la liste de référence."));

        Producteur producteur = producteurs.parParcelleId(parcelleId)
                .orElseThrow(() -> new RessourceIntrouvableException("Parcelle introuvable."));
        Parcelle parcelle = producteur.parcelles().stream()
                .filter(p -> p.id().equals(parcelleId))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Parcelle introuvable."));

        BigDecimal dejaEmblave = cultures.sommeSuperficiesEmblavees(parcelleId, campagneId);
        if (dejaEmblave.add(superficieEmblavee).compareTo(parcelle.superficie()) > 0) {
            throw new RegleMetierException("SUPERFICIE_PARCELLE_DEPASSEE",
                    "La superficie emblavée cumulée dépasserait la superficie de la parcelle ("
                            + parcelle.superficie().toPlainString() + " ha).");
        }

        ParametresCulture parametres = new ParametresCulture(
                referentiel.rendementKgHa(), referentiel.humiditeMax(), referentiel.cycleMinJours());
        Culture culture = Culture.planifier(UUID.randomUUID(), parcelleId, producteur.id(), campagneId, type,
                superficieEmblavee, parametres);
        return cultures.enregistrer(culture);
    }

    public Culture semer(Utilisateur utilisateur, UUID cultureId, LocalDate dateSemis) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Culture culture = charger(cultureId);
        exigerCampagneOuverte(culture);
        culture.semer(dateSemis);
        return cultures.enregistrer(culture);
    }

    public Culture recolter(Utilisateur utilisateur, UUID cultureId, LocalDate dateRecolte, BigDecimal quantiteKg) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Culture culture = charger(cultureId);
        exigerCampagneOuverte(culture);
        culture.recolter(dateRecolte, quantiteKg);
        return cultures.enregistrer(culture);
    }

    public Culture detail(Utilisateur utilisateur, UUID cultureId) {
        Culture culture = charger(cultureId);
        utilisateurs.verifierAccesProducteur(utilisateur, culture.producteurId().toString());
        return culture;
    }

    public List<Culture> parProducteur(Utilisateur utilisateur, UUID producteurId) {
        utilisateurs.verifierAccesProducteur(utilisateur, producteurId.toString());
        return cultures.parProducteur(producteurId);
    }

    private Culture charger(UUID cultureId) {
        return cultures.parId(cultureId)
                .orElseThrow(() -> new RessourceIntrouvableException("Culture introuvable."));
    }

    private void exigerCampagneOuverte(Culture culture) {
        Campagne campagne = campagnes.parId(culture.campagneId())
                .orElseThrow(() -> new RessourceIntrouvableException("Campagne introuvable."));
        campagne.exigerOuverte();
    }
}
