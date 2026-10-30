package sn.sunagri.entrepot.application;

import java.math.BigDecimal;
import java.util.List;
import sn.sunagri.entrepot.domain.LotConsomme;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

/**
 * Port publié par le contexte entrepôt à destination des autres contextes.
 */
public interface StockPort {

    BigDecimal quantiteDisponible(TypeCulture typeCulture, Qualite qualite);

    List<LotConsomme> consommer(TypeCulture typeCulture, Qualite qualite, BigDecimal quantiteKg);
}
