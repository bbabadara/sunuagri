package sn.sunagri.production.application;

import java.util.List;
import java.util.UUID;

public record RapportImportProducteurs(
        int totalLignes,
        int acceptees,
        int refusees,
        List<ProducteurImporte> producteurs,
        List<LigneRejetee> rejets) {

    public record ProducteurImporte(
            int ligne,
            UUID id,
            String nom,
            String prenom,
            String telephone,
            String village) {
    }

    public record LigneRejetee(int ligne, String motif) {
    }
}
