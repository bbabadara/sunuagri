package sn.sunagri.production.adapter.out.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.production.application.port.out.CampagneRepository;
import sn.sunagri.production.domain.Campagne;
import sn.sunagri.production.domain.Saison;

@Repository
public class InMemoryCampagneRepository implements CampagneRepository {

    private final Map<UUID, Campagne> campagnes = new ConcurrentHashMap<>();

    @Override
    public Campagne enregistrer(Campagne campagne) {
        campagnes.put(campagne.id(), campagne);
        return campagne;
    }

    @Override
    public Optional<Campagne> parId(UUID id) {
        return Optional.ofNullable(campagnes.get(id));
    }

    @Override
    public boolean existe(Saison saison, int annee) {
        return campagnes.values().stream()
                .anyMatch(campagne -> campagne.saison() == saison && campagne.annee() == annee);
    }

    @Override
    public List<Campagne> toutes() {
        return List.copyOf(campagnes.values());
    }
}
