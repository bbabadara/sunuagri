package sn.sunagri.commercialisation.adapter.out.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.commercialisation.application.port.out.OffreRepository;
import sn.sunagri.commercialisation.domain.Offre;

@Repository
public class InMemoryOffreRepository implements OffreRepository {

    private final Map<UUID, Offre> offres = new ConcurrentHashMap<>();

    @Override
    public Offre enregistrer(Offre offre) {
        offres.put(offre.id(), offre);
        return offre;
    }

    @Override
    public Optional<Offre> parId(UUID id) {
        return Optional.ofNullable(offres.get(id));
    }

    @Override
    public List<Offre> toutes() {
        return List.copyOf(offres.values());
    }
}
