package sn.sunagri.commercialisation.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ResultatRepartition(BigDecimal commissionXof, Map<UUID, BigDecimal> montantsParProducteur) {
}
