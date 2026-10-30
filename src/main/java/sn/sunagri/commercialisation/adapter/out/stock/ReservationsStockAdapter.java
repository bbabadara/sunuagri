package sn.sunagri.commercialisation.adapter.out.stock;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import sn.sunagri.commercialisation.application.port.out.OffreRepository;
import sn.sunagri.commercialisation.domain.Offre;
import sn.sunagri.entrepot.application.port.out.ReservationsStockPort;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

@Component
public class ReservationsStockAdapter implements ReservationsStockPort {

    private final OffreRepository offres;

    public ReservationsStockAdapter(OffreRepository offres) {
        this.offres = offres;
    }

    @Override
    public BigDecimal quantiteReservee(TypeCulture typeCulture, Qualite qualite) {
        return offres.toutes().stream()
                .filter(offre -> offre.typeCulture() == typeCulture && offre.qualite() == qualite)
                .map(Offre::quantiteReservee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
