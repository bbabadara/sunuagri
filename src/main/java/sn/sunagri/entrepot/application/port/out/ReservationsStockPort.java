package sn.sunagri.entrepot.application.port.out;

import java.math.BigDecimal;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

/**
 * Quantité réservée par les offres de vente en cours. Implémenté par le contexte
 * commercialisation, consommé par le contexte entrepôt.
 */
public interface ReservationsStockPort {

    BigDecimal quantiteReservee(TypeCulture typeCulture, Qualite qualite);
}
