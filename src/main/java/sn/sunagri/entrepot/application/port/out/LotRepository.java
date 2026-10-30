package sn.sunagri.entrepot.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.entrepot.domain.Lot;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

public interface LotRepository {

    Lot enregistrer(Lot lot);

    Optional<Lot> parId(UUID id);

    List<Lot> tous();

    List<Lot> parCultureEtQualiteTriesParDate(TypeCulture typeCulture, Qualite qualite);
}
