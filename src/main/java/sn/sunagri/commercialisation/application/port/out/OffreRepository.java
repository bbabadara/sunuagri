package sn.sunagri.commercialisation.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.commercialisation.domain.Offre;

public interface OffreRepository {

    Offre enregistrer(Offre offre);

    Optional<Offre> parId(UUID id);

    List<Offre> toutes();
}
