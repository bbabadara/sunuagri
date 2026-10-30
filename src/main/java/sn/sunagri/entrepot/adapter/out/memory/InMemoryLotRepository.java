package sn.sunagri.entrepot.adapter.out.memory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.entrepot.application.port.out.LotRepository;
import sn.sunagri.entrepot.domain.Lot;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

@Repository
public class InMemoryLotRepository implements LotRepository {

    private final Map<UUID, Lot> lots = new ConcurrentHashMap<>();

    @Override
    public Lot enregistrer(Lot lot) {
        lots.put(lot.id(), lot);
        return lot;
    }

    @Override
    public Optional<Lot> parId(UUID id) {
        return Optional.ofNullable(lots.get(id));
    }

    @Override
    public List<Lot> tous() {
        return List.copyOf(lots.values());
    }

    @Override
    public List<Lot> parCultureEtQualiteTriesParDate(TypeCulture typeCulture, Qualite qualite) {
        return lots.values().stream()
                .filter(lot -> lot.typeCulture() == typeCulture && lot.qualite() == qualite)
                .sorted(Comparator.comparing(Lot::dateReception).thenComparing(Lot::id))
                .toList();
    }
}
