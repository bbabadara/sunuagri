package sn.sunagri.entrepot.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record LotConsomme(UUID lotId, UUID producteurId, BigDecimal quantiteKg) {
}
