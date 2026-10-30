package sn.sunagri.commercialisation.adapter.out.memory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;
import sn.sunagri.commercialisation.application.port.out.CommandeRepository;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.StatutCommande;

@Repository
public class InMemoryCommandeRepository implements CommandeRepository {

    private final Map<UUID, Commande> commandes = new ConcurrentHashMap<>();

    @Override
    public Commande enregistrer(Commande commande) {
        commandes.put(commande.id(), commande);
        return commande;
    }

    @Override
    public Optional<Commande> parId(UUID id) {
        return Optional.ofNullable(commandes.get(id));
    }

    @Override
    public List<Commande> rechercher(String acheteurId, StatutCommande statut, int page, int size) {
        return filtrer(acheteurId, statut)
                .sorted(Comparator.comparing(Commande::dateCommande).reversed()
                        .thenComparing(commande -> commande.id().toString()))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public long compter(String acheteurId, StatutCommande statut) {
        return filtrer(acheteurId, statut).count();
    }

    @Override
    public List<Commande> toutes() {
        return List.copyOf(commandes.values());
    }

    private Stream<Commande> filtrer(String acheteurId, StatutCommande statut) {
        return commandes.values().stream()
                .filter(commande -> acheteurId == null || commande.acheteurId().equals(acheteurId))
                .filter(commande -> statut == null || commande.statut() == statut);
    }
}
