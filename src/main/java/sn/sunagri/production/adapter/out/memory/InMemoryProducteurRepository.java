package sn.sunagri.production.adapter.out.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import sn.sunagri.production.application.port.out.ProducteurRepository;
import sn.sunagri.production.domain.Parcelle;
import sn.sunagri.production.domain.Producteur;

@Repository
public class InMemoryProducteurRepository implements ProducteurRepository {

    private final Map<UUID, Producteur> producteurs = new ConcurrentHashMap<>();

    @Override
    public Producteur enregistrer(Producteur producteur) {
        producteurs.put(producteur.id(), producteur);
        return producteur;
    }

    @Override
    public Optional<Producteur> parId(UUID id) {
        return Optional.ofNullable(producteurs.get(id));
    }

    @Override
    public Optional<Producteur> parTelephone(String telephone) {
        return producteurs.values().stream()
                .filter(producteur -> producteur.telephone().equals(telephone))
                .findFirst();
    }

    @Override
    public Optional<Producteur> parParcelleId(UUID parcelleId) {
        return producteurs.values().stream()
                .filter(producteur -> producteur.parcelles().stream()
                        .map(Parcelle::id)
                        .anyMatch(parcelleId::equals))
                .findFirst();
    }

    @Override
    public boolean existeTelephone(String telephone) {
        return parTelephone(telephone).isPresent();
    }

    @Override
    public List<Producteur> tous() {
        return List.copyOf(producteurs.values());
    }
}
