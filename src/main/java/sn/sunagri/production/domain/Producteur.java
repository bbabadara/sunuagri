package sn.sunagri.production.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sn.sunagri.shared.domain.ConflitException;
import sn.sunagri.shared.domain.RegleMetierException;
import sn.sunagri.shared.domain.Validations;

public class Producteur {

    private final UUID id;
    private final String nom;
    private final String prenom;
    private final String telephone;
    private final String village;
    private final List<Parcelle> parcelles = new ArrayList<>();

    private Producteur(UUID id, String nom, String prenom, String telephone, String village) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant du producteur est obligatoire.");
        }
        this.id = id;
        this.nom = Validations.texteObligatoire(nom, "nom");
        this.prenom = Validations.texteObligatoire(prenom, "prénom");
        this.telephone = Validations.texteObligatoire(telephone, "téléphone");
        this.village = Validations.texteObligatoire(village, "village");
    }

    public static Producteur creer(UUID id, String nom, String prenom, String telephone, String village) {
        return new Producteur(id, nom, prenom, telephone, village);
    }

    public Parcelle ajouterParcelle(UUID parcelleId, String nomParcelle, BigDecimal superficie,
            String commune, BigDecimal superficieMaxProducteur) {
        if (superficieMaxProducteur == null) {
            throw new IllegalArgumentException("La superficie maximale par producteur est obligatoire.");
        }
        Parcelle parcelle = new Parcelle(parcelleId, nomParcelle, superficie, commune);
        boolean nomDejaUtilise = parcelles.stream()
                .anyMatch(existante -> existante.nom().equalsIgnoreCase(parcelle.nom()));
        if (nomDejaUtilise) {
            throw new ConflitException("PARCELLE_DEJA_DECLAREE",
                    "Une parcelle nommée « " + parcelle.nom() + " » existe déjà pour ce producteur.");
        }
        BigDecimal nouveauTotal = superficieTotale().add(parcelle.superficie());
        if (nouveauTotal.compareTo(superficieMaxProducteur) > 0) {
            throw new RegleMetierException("SUPERFICIE_MAX_PRODUCTEUR_DEPASSEE",
                    "Le total des superficies du producteur dépasserait " + superficieMaxProducteur.toPlainString()
                            + " ha.");
        }
        parcelles.add(parcelle);
        return parcelle;
    }

    public BigDecimal superficieTotale() {
        return parcelles.stream()
                .map(Parcelle::superficie)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID id() {
        return id;
    }

    public String nom() {
        return nom;
    }

    public String prenom() {
        return prenom;
    }

    public String telephone() {
        return telephone;
    }

    public String village() {
        return village;
    }

    public List<Parcelle> parcelles() {
        return List.copyOf(parcelles);
    }
}
