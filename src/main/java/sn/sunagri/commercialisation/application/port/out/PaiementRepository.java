package sn.sunagri.commercialisation.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.commercialisation.domain.PaiementDu;

public interface PaiementRepository {

    PaiementDu enregistrer(PaiementDu paiement);

    Optional<PaiementDu> parId(UUID id);

    List<PaiementDu> parCommande(UUID commandeId);

    List<PaiementDu> parProducteur(UUID producteurId);

    List<PaiementDu> tous();
}
