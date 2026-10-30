package sn.sunagri.commercialisation.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.StatutCommande;

public interface CommandeRepository {

    Commande enregistrer(Commande commande);

    Optional<Commande> parId(UUID id);

    /**
     * @param acheteurId filtre acheteur, ou {@code null} pour toutes les commandes
     * @param statut     filtre statut, ou {@code null} pour tous les statuts
     */
    List<Commande> rechercher(String acheteurId, StatutCommande statut, int page, int size);

    long compter(String acheteurId, StatutCommande statut);

    List<Commande> toutes();
}
