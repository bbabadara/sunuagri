package sn.sunagri.production.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.service.CampagneService;
import sn.sunagri.production.domain.Campagne;
import sn.sunagri.production.domain.Saison;
import sn.sunagri.production.domain.StatutCampagne;

@RestController
@RequestMapping("/api/v1/campagnes")
@Tag(name = "Campagnes", description = "Campagnes agricoles")
public class CampagneController {

    private final CampagneService service;
    private final CurrentUserService utilisateurs;

    public CampagneController(CampagneService service, CurrentUserService utilisateurs) {
        this.service = service;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping
    public ResponseEntity<CampagneResponse> ouvrir(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @Valid @RequestBody OuvrirCampagneRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Campagne campagne = service.ouvrir(utilisateur, requete.saison(), requete.annee(),
                requete.dateDebut(), requete.dateFin());
        return ResponseEntity.created(URI.create("/api/v1/campagnes/" + campagne.id()))
                .body(CampagneResponse.depuis(campagne));
    }

    @PostMapping("/{campagneId}/cloture")
    public CampagneResponse cloturer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID campagneId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return CampagneResponse.depuis(service.cloturer(utilisateur, campagneId));
    }

    @GetMapping
    public List<CampagneResponse> toutes(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return service.toutes(utilisateur).stream().map(CampagneResponse::depuis).toList();
    }

    public record OuvrirCampagneRequest(
            @NotNull(message = "La saison est obligatoire") Saison saison,
            @Min(value = 2000, message = "L'année doit être valide") int annee,
            @NotNull(message = "La date de début est obligatoire") LocalDate dateDebut,
            @NotNull(message = "La date de fin est obligatoire") LocalDate dateFin) {
    }

    public record CampagneResponse(
            UUID id,
            Saison saison,
            int annee,
            LocalDate dateDebut,
            LocalDate dateFin,
            StatutCampagne statut) {

        static CampagneResponse depuis(Campagne campagne) {
            return new CampagneResponse(campagne.id(), campagne.saison(), campagne.annee(),
                    campagne.dateDebut(), campagne.dateFin(), campagne.statut());
        }
    }
}
