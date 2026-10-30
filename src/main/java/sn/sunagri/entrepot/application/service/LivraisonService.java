package sn.sunagri.entrepot.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sn.sunagri.entrepot.application.port.out.LivraisonRepository;
import sn.sunagri.entrepot.application.port.out.LotRepository;
import sn.sunagri.entrepot.domain.Livraison;
import sn.sunagri.entrepot.domain.Lot;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.port.out.CultureRepository;
import sn.sunagri.production.domain.Culture;
import sn.sunagri.shared.domain.ClockPort;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.RessourceIntrouvableException;
import sn.sunagri.shared.domain.Validations;

@Service
public class LivraisonService {

    private final CultureRepository cultures;
    private final LivraisonRepository livraisons;
    private final LotRepository lots;
    private final ClockPort horloge;
    private final CurrentUserService utilisateurs;

    public LivraisonService(CultureRepository cultures, LivraisonRepository livraisons, LotRepository lots,
            ClockPort horloge, CurrentUserService utilisateurs) {
        this.cultures = cultures;
        this.livraisons = livraisons;
        this.lots = lots;
        this.horloge = horloge;
        this.utilisateurs = utilisateurs;
    }

    public Livraison livrer(Utilisateur utilisateur, UUID cultureId, BigDecimal quantiteKg,
            BigDecimal tauxHumidite, Qualite qualite) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        if (qualite == null) {
            throw new RegleMetierException("QUALITE_OBLIGATOIRE", "La qualité de la livraison est obligatoire.");
        }
        Culture culture = cultures.parId(cultureId)
                .orElseThrow(() -> new RessourceIntrouvableException("Culture introuvable."));
        if (!culture.estRecoltee()) {
            throw new RegleMetierException("CULTURE_NON_RECOLTEE",
                    "Seule une culture récoltée peut être livrée à l'entrepôt.");
        }
        Validations.strictementPositif(quantiteKg, "quantité livrée");
        Validations.positifOuNul(tauxHumidite, "taux d'humidité");

        BigDecimal humiditeMax = culture.parametres().humiditeMax();
        if (tauxHumidite.compareTo(humiditeMax) > 0) {
            throw new RegleMetierException("HUMIDITE_TROP_ELEVEE",
                    "Le taux d'humidité de " + tauxHumidite.toPlainString() + " % dépasse le seuil de "
                            + humiditeMax.toPlainString() + " % pour cette culture.");
        }
        BigDecimal dejaLivre = livraisons.sommeQuantitesParCulture(cultureId);
        if (dejaLivre.add(quantiteKg).compareTo(culture.quantiteRecoltee()) > 0) {
            throw new RegleMetierException("QUANTITE_LIVREE_SUPERIEURE",
                    "Le cumul des livraisons dépasserait la quantité récoltée de "
                            + culture.quantiteRecoltee().toPlainString() + " kg.");
        }

        LocalDate date = horloge.aujourdHui();
        UUID livraisonId = UUID.randomUUID();
        Lot lot = Lot.creer(UUID.randomUUID(), livraisonId, culture.producteurId(), culture.type(), qualite,
                quantiteKg, date);
        lots.enregistrer(lot);

        Livraison livraison = new Livraison(livraisonId, cultureId, culture.producteurId(), culture.type(),
                qualite, quantiteKg, tauxHumidite, date, lot.id());
        return livraisons.enregistrer(livraison);
    }
}
