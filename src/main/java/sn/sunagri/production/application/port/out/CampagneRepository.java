package sn.sunagri.production.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.production.domain.Campagne;
import sn.sunagri.production.domain.Saison;

public interface CampagneRepository {

    Campagne enregistrer(Campagne campagne);

    Optional<Campagne> parId(UUID id);

    boolean existe(Saison saison, int annee);

    List<Campagne> toutes();
}
