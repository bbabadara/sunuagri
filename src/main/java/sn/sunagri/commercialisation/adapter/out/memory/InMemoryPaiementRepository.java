package sn.sunagri.commercialisation.adapter.out.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.commercialisation.application.port.out.PaiementRepository;
import sn.sunagri.commercialisation.domain.PaiementDu;

@Repository
public class InMemoryPaiementRepository implements PaiementRepository {

    private final Map<UUID, PaiementDu> paiements = new ConcurrentHashMap<>();

    @Override
    public PaiementDu enregistrer(PaiementDu paiement) {
        paiements.put(paiement.id(), paiement);
        return paiement;
    }

    @Override
    public Optional<PaiementDu> parId(UUID id) {
        return Optional.ofNullable(paiements.get(id));
    }

    @Override
    public List<PaiementDu> parCommande(UUID commandeId) {
        return paiements.values().stream()
                .filter(paiement -> paiement.commandeId().equals(commandeId))
                .toList();
    }

    @Override
    public List<PaiementDu> parProducteur(UUID producteurId) {
        return paiements.values().stream()
                .filter(paiement -> paiement.producteurId().equals(producteurId))
                .toList();
    }

    @Override
    public List<PaiementDu> tous() {
        return List.copyOf(paiements.values());
    }
}
