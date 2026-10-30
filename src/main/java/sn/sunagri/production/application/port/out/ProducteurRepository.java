package sn.sunagri.production.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.production.domain.Producteur;

public interface ProducteurRepository {

    Producteur enregistrer(Producteur producteur);

    Optional<Producteur> parId(UUID id);

    Optional<Producteur> parTelephone(String telephone);

    Optional<Producteur> parParcelleId(UUID parcelleId);

    boolean existeTelephone(String telephone);

    List<Producteur> tous();
}
