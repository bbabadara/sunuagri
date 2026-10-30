package sn.sunagri.production.application;

import java.util.Optional;
import java.util.UUID;

/**
 * Port publié par le contexte production : accès en lecture aux coordonnées d'un
 * producteur pour les autres contextes (ex. notification).
 */
public interface ProducteurDirectoryPort {

    Optional<ContactProducteur> contact(UUID producteurId);
}
