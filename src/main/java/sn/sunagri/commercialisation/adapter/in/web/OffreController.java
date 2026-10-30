package sn.sunagri.commercialisation.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.sunagri.commercialisation.application.service.OffreService;
import sn.sunagri.commercialisation.domain.Offre;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

@RestController
@RequestMapping("/api/v1/offres")
@Tag(name = "Offres", description = "Offres de vente de la coopérative")
public class OffreController {

    private final OffreService service;
    private final CurrentUserService utilisateurs;

    public OffreController(OffreService service, CurrentUserService utilisateurs) {
        this.service = service;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping
    public ResponseEntity<OffreResponse> publier(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @Valid @RequestBody PublierOffreRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Offre offre = service.publier(utilisateur, requete.typeCulture(), requete.qualite(),
                requete.quantiteKg(), requete.prixKg());
        return ResponseEntity.created(URI.create("/api/v1/offres/" + offre.id()))
                .body(OffreResponse.depuis(offre));
    }

    @GetMapping
    public List<OffreResponse> lister(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @RequestParam(required = false) TypeCulture typeCulture,
            @RequestParam(required = false) Qualite qualite) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return service.lister(utilisateur, typeCulture, qualite).stream()
                .map(OffreResponse::depuis)
                .toList();
    }

    @GetMapping("/{offreId}")
    public OffreResponse detail(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID offreId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return OffreResponse.depuis(service.detail(utilisateur, offreId));
    }

    @PostMapping("/{offreId}/retrait")
    public OffreResponse retirer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID offreId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return OffreResponse.depuis(service.retirer(utilisateur, offreId));
    }

    public record PublierOffreRequest(
            @NotNull(message = "La culture est obligatoire") TypeCulture typeCulture,
            @NotNull(message = "La qualité est obligatoire") Qualite qualite,
            @NotNull(message = "La quantité est obligatoire")
            @DecimalMin(value = "0.01", message = "La quantité doit être strictement positive") BigDecimal quantiteKg,
            @NotNull(message = "Le prix au kilogramme est obligatoire")
            @DecimalMin(value = "0.01", message = "Le prix doit être strictement positif") BigDecimal prixKg) {
    }
}
