package sn.sunagri.entrepot.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import sn.sunagri.entrepot.application.StockPort;
import sn.sunagri.entrepot.application.port.out.LotRepository;
import sn.sunagri.entrepot.application.port.out.ReservationsStockPort;
import sn.sunagri.entrepot.domain.Lot;
import sn.sunagri.entrepot.domain.LotConsomme;
import sn.sunagri.entrepot.domain.StockLigne;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.TypeCulture;
import sn.sunagri.shared.domain.Validations;

@Service
public class StockService implements StockPort {

    private final LotRepository lots;
    private final ReservationsStockPort reservations;
    private final CurrentUserService utilisateurs;

    public StockService(LotRepository lots, ReservationsStockPort reservations, CurrentUserService utilisateurs) {
        this.lots = lots;
        this.reservations = reservations;
        this.utilisateurs = utilisateurs;
    }

    public List<StockLigne> consulter(Utilisateur utilisateur, TypeCulture typeCulture, Qualite qualite) {
        utilisateurs.exigerAgentOuAdmin(utilisateur);
        Map<TypeCulture, Map<Qualite, BigDecimal>> totaux = new EnumMap<>(TypeCulture.class);
        for (Lot lot : lots.tous()) {
            totaux.computeIfAbsent(lot.typeCulture(), type -> new EnumMap<>(Qualite.class))
                    .merge(lot.qualite(), lot.quantiteDisponible(), BigDecimal::add);
        }
        List<StockLigne> lignes = new ArrayList<>();
        totaux.forEach((typeCourant, parQualite) -> parQualite.forEach((qualiteCourante, total) -> {
            if (typeCulture != null && typeCulture != typeCourant) {
                return;
            }
            if (qualite != null && qualite != qualiteCourante) {
                return;
            }
            BigDecimal reserve = reservations.quantiteReservee(typeCourant, qualiteCourante);
            BigDecimal disponible = total.subtract(reserve).max(BigDecimal.ZERO);
            lignes.add(new StockLigne(typeCourant, qualiteCourante, total, reserve, disponible));
        }));
        lignes.sort(Comparator.comparing(StockLigne::typeCulture).thenComparing(StockLigne::qualite));
        return lignes;
    }

    @Override
    public BigDecimal quantiteDisponible(TypeCulture typeCulture, Qualite qualite) {
        exigerCles(typeCulture, qualite);
        BigDecimal total = sommeDisponible(lots.parCultureEtQualiteTriesParDate(typeCulture, qualite));
        BigDecimal reserve = reservations.quantiteReservee(typeCulture, qualite);
        return total.subtract(reserve).max(BigDecimal.ZERO);
    }

    @Override
    public List<LotConsomme> consommer(TypeCulture typeCulture, Qualite qualite, BigDecimal quantiteKg) {
        exigerCles(typeCulture, qualite);
        Validations.strictementPositif(quantiteKg, "quantité à consommer");
        List<Lot> candidats = lots.parCultureEtQualiteTriesParDate(typeCulture, qualite);
        BigDecimal disponible = sommeDisponible(candidats);
        if (disponible.compareTo(quantiteKg) < 0) {
            throw new RegleMetierException("STOCK_INSUFFISANT",
                    "Le stock disponible de " + typeCulture + " qualité " + qualite + " est de "
                            + disponible.toPlainString() + " kg, insuffisant pour " + quantiteKg.toPlainString()
                            + " kg.");
        }
        BigDecimal restant = quantiteKg;
        List<LotConsomme> consommes = new ArrayList<>();
        for (Lot lot : candidats) {
            if (restant.signum() <= 0) {
                break;
            }
            BigDecimal prelevement = restant.min(lot.quantiteDisponible());
            if (prelevement.signum() > 0) {
                lot.consommer(prelevement);
                lots.enregistrer(lot);
                consommes.add(new LotConsomme(lot.id(), lot.producteurId(), prelevement));
                restant = restant.subtract(prelevement);
            }
        }
        return consommes;
    }

    private BigDecimal sommeDisponible(List<Lot> lotsDisponibles) {
        return lotsDisponibles.stream()
                .map(Lot::quantiteDisponible)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void exigerCles(TypeCulture typeCulture, Qualite qualite) {
        if (typeCulture == null || qualite == null) {
            throw new RegleMetierException("CLE_STOCK_INVALIDE",
                    "La culture et la qualité sont obligatoires pour interroger le stock.");
        }
    }
}
