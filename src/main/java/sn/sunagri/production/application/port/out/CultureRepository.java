package sn.sunagri.production.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.production.domain.Culture;

public interface CultureRepository {

    Culture enregistrer(Culture culture);

    Optional<Culture> parId(UUID id);

    BigDecimal sommeSuperficiesEmblavees(UUID parcelleId, UUID campagneId);

    List<Culture> parProducteur(UUID producteurId);
}
