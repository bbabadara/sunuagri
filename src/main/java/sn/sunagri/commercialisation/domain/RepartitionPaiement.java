package sn.sunagri.commercialisation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import sn.sunagri.shared.domain.RegleMetierException;

/**
 * Répartit un montant entre producteurs au prorata des quantités apportées, après
 * retenue de la commission de la coopérative. Les francs restants sont attribués par
 * la méthode des plus forts restes (ADR-0004).
 */
public final class RepartitionPaiement {

    private static final int PRECISION = 10;

    private RepartitionPaiement() {
    }

    public static ResultatRepartition calculer(BigDecimal montantTotalXof, BigDecimal tauxCommission,
            Map<UUID, BigDecimal> quantitesParProducteur) {
        if (montantTotalXof == null || montantTotalXof.signum() < 0) {
            throw new RegleMetierException("MONTANT_INVALIDE", "Le montant à répartir doit être positif ou nul.");
        }
        if (tauxCommission == null || tauxCommission.signum() < 0 || tauxCommission.compareTo(BigDecimal.ONE) > 0) {
            throw new RegleMetierException("TAUX_COMMISSION_INVALIDE",
                    "Le taux de commission doit être compris entre 0 et 1.");
        }
        BigDecimal totalQuantites = quantitesParProducteur.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalQuantites.signum() <= 0) {
            throw new RegleMetierException("QUANTITES_VIDES",
                    "Aucune quantité n'a été apportée : impossible de répartir le paiement.");
        }

        BigDecimal commission = montantTotalXof.multiply(tauxCommission).setScale(0, RoundingMode.HALF_UP);
        if (commission.compareTo(montantTotalXof) > 0) {
            commission = montantTotalXof;
        }
        BigDecimal reste = montantTotalXof.subtract(commission);

        Map<UUID, BigDecimal> parts = new LinkedHashMap<>();
        Map<UUID, BigDecimal> restes = new LinkedHashMap<>();
        BigDecimal distribue = BigDecimal.ZERO;
        List<UUID> producteurs = quantitesParProducteur.keySet().stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        for (UUID producteurId : producteurs) {
            BigDecimal quantite = quantitesParProducteur.get(producteurId);
            if (quantite == null || quantite.signum() < 0) {
                throw new RegleMetierException("QUANTITE_INVALIDE",
                        "Une quantité apportée ne peut pas être négative.");
            }
            BigDecimal partExacte = reste.multiply(quantite)
                    .divide(totalQuantites, PRECISION, RoundingMode.HALF_UP);
            BigDecimal partEntiere = partExacte.setScale(0, RoundingMode.FLOOR);
            parts.put(producteurId, partEntiere);
            restes.put(producteurId, partExacte.subtract(partEntiere));
            distribue = distribue.add(partEntiere);
        }

        long aDistribuer = reste.subtract(distribue).longValueExact();
        List<UUID> ordre = restes.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed()
                        .thenComparing(entree -> entree.getKey().toString()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < aDistribuer; i++) {
            UUID producteurId = ordre.get(i);
            parts.put(producteurId, parts.get(producteurId).add(BigDecimal.ONE));
        }

        return new ResultatRepartition(commission, Map.copyOf(parts));
    }
}
