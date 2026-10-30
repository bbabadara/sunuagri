package sn.sunagri.entrepot.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.entrepot.domain.Livraison;

public interface LivraisonRepository {

    Livraison enregistrer(Livraison livraison);

    Optional<Livraison> parId(UUID id);

    BigDecimal sommeQuantitesParCulture(UUID cultureId);

    List<Livraison> toutes();
}
