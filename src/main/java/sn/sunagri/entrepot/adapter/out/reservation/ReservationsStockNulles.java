package sn.sunagri.entrepot.adapter.out.reservation;

import java.math.BigDecimal;
import org.springframework.stereotype.Repository;
import sn.sunagri.entrepot.application.port.out.ReservationsStockPort;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

/**
 * Implémentation transitoire : aucune offre n'existe encore avant le contexte
 * commercialisation.
 */
@Repository
public class ReservationsStockNulles implements ReservationsStockPort {

    @Override
    public BigDecimal quantiteReservee(TypeCulture typeCulture, Qualite qualite) {
        return BigDecimal.ZERO;
    }
}
