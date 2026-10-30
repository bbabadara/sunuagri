package sn.sunagri.referentiel.application.port.out;

import java.util.List;
import java.util.Optional;
import sn.sunagri.referentiel.domain.CultureReferentiel;
import sn.sunagri.shared.domain.TypeCulture;

public interface CultureReferentielRepository {

    Optional<CultureReferentiel> parType(TypeCulture type);

    List<CultureReferentiel> toutes();
}
