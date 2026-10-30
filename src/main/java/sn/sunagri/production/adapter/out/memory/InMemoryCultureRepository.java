package sn.sunagri.production.adapter.out.memory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.production.application.port.out.CultureRepository;
import sn.sunagri.production.domain.Culture;

@Repository
public class InMemoryCultureRepository implements CultureRepository {

    private final Map<UUID, Culture> cultures = new ConcurrentHashMap<>();

    @Override
    public Culture enregistrer(Culture culture) {
        cultures.put(culture.id(), culture);
        return culture;
    }

    @Override
    public Optional<Culture> parId(UUID id) {
        return Optional.ofNullable(cultures.get(id));
    }

    @Override
    public BigDecimal sommeSuperficiesEmblavees(UUID parcelleId, UUID campagneId) {
        return cultures.values().stream()
                .filter(culture -> culture.parcelleId().equals(parcelleId)
                        && culture.campagneId().equals(campagneId))
                .map(Culture::superficieEmblavee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Culture> parProducteur(UUID producteurId) {
        return cultures.values().stream()
                .filter(culture -> culture.producteurId().equals(producteurId))
                .toList();
    }
}
