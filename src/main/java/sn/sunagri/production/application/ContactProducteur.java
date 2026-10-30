package sn.sunagri.production.application;

import java.util.UUID;

public record ContactProducteur(UUID id, String nom, String prenom, String telephone) {
}
