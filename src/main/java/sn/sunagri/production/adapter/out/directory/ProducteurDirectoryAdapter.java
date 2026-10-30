package sn.sunagri.production.adapter.out.directory;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import sn.sunagri.production.application.ContactProducteur;
import sn.sunagri.production.application.ProducteurDirectoryPort;
import sn.sunagri.production.application.port.out.ProducteurRepository;

@Component
public class ProducteurDirectoryAdapter implements ProducteurDirectoryPort {

    private final ProducteurRepository producteurs;

    public ProducteurDirectoryAdapter(ProducteurRepository producteurs) {
        this.producteurs = producteurs;
    }

    @Override
    public Optional<ContactProducteur> contact(UUID producteurId) {
        return producteurs.parId(producteurId)
                .map(produit -> new ContactProducteur(produit.id(), produit.nom(), produit.prenom(),
                        produit.telephone()));
    }
}
