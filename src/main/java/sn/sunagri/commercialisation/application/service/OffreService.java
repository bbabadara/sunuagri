package sn.sunagri.commercialisation.application.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.commercialisation.application.port.out.OffreRepository;
import sn.sunagri.commercialisation.domain.Offre;
import sn.sunagri.entrepot.application.StockPort;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Role;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.ClockPort;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

@Service
public class OffreService {

    private final OffreRepository offres;
    private final StockPort stock;
    private final CurrentUserService utilisateurs;
    private final ClockPort horloge;

    public OffreService(OffreRepository offres, StockPort stock, CurrentUserService utilisateurs, ClockPort horloge) {
        this.offres = offres;
        this.stock = stock;
        this.utilisateurs = utilisateurs;
        this.horloge = horloge;
    }

    public Offre publier(Utilisateur utilisateur, TypeCulture typeCulture, Qualite qualite, BigDecimal quantiteKg,
            BigDecimal prixKg) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        if (typeCulture == null || qualite == null) {
            throw new RegleMetierException("OFFRE_INVALIDE", "La culture et la qualité sont obligatoires.");
        }
        Validations.strictementPositif(quantiteKg, "quantité de l'offre");
        Validations.strictementPositif(prixKg, "prix au kilogramme");
        BigDecimal disponible = stock.quantiteDisponible(typeCulture, qualite);
        if (quantiteKg.compareTo(disponible) > 0) {
            throw new RegleMetierException("OFFRE_SUPERIEURE_AU_STOCK",
                    "Le stock disponible de " + typeCulture + " qualité " + qualite + " est de "
                            + disponible.toPlainString() + " kg, insuffisant pour une offre de "
                            + quantiteKg.toPlainString() + " kg.");
        }
        Offre offre = Offre.publier(UUID.randomUUID(), typeCulture, qualite, quantiteKg, prixKg,
                horloge.aujourdHui());
        return offres.enregistrer(offre);
    }

    public Offre retirer(Utilisateur utilisateur, UUID offreId) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Offre offre = charger(offreId);
        offre.retirer();
        return offres.enregistrer(offre);
    }

    public List<Offre> lister(Utilisateur utilisateur, TypeCulture typeCulture, Qualite qualite) {
        utilisateurs.exigerRole(utilisateur, Role.ACHETEUR, Role.AGENT, Role.ADMIN);
        return offres.toutes().stream()
                .filter(offre -> typeCulture == null || offre.typeCulture() == typeCulture)
                .filter(offre -> qualite == null || offre.qualite() == qualite)
                .filter(offre -> utilisateur.estAgentOuAdmin() || offre.estOuverte())
                .sorted(Comparator.comparing(Offre::dateCreation).reversed()
                        .thenComparing(offre -> offre.id().toString()))
                .toList();
    }

    public Offre detail(Utilisateur utilisateur, UUID offreId) {
        utilisateurs.exigerRole(utilisateur, Role.ACHETEUR, Role.AGENT, Role.ADMIN);
        Offre offre = charger(offreId);
        if (!utilisateur.estAgentOuAdmin() && !offre.estOuverte()) {
            throw new RessourceIntrouvableException("Offre introuvable.");
        }
        return offre;
    }

    private Offre charger(UUID offreId) {
        return offres.parId(offreId)
                .orElseThrow(() -> new RessourceIntrouvableException("Offre introuvable."));
    }
}
