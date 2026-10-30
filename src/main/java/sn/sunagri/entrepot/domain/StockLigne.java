package sn.sunagri.entrepot.domain;

import java.math.BigDecimal;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

public record StockLigne(
        TypeCulture typeCulture,
        Qualite qualite,
        BigDecimal totalKg,
        BigDecimal reserveKg,
        BigDecimal disponibleKg) {
}
