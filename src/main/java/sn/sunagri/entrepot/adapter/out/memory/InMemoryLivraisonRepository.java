package sn.sunagri.entrepot.adapter.out.memory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.entrepot.application.port.out.LivraisonRepository;
import sn.sunagri.entrepot.domain.Livraison;

@Repository
public class InMemoryLivraisonRepository implements LivraisonRepository {

    private final Map<UUID, Livraison> livraisons = new ConcurrentHashMap<>();

    @Override
    public Livraison enregistrer(Livraison livraison) {
        livraisons.put(livraison.id(), livraison);
        return livraison;
    }

    @Override
    public Optional<Livraison> parId(UUID id) {
        return Optional.ofNullable(livraisons.get(id));
    }

    @Override
    public BigDecimal sommeQuantitesParCulture(UUID cultureId) {
        return livraisons.values().stream()
                .filter(livraison -> livraison.cultureId().equals(cultureId))
                .map(Livraison::quantiteKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Livraison> toutes() {
        return List.copyOf(livraisons.values());
    }
}
