package sn.sunagri.production.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
import sn.sunagri.production.application.service.CultureService;
import sn.sunagri.production.domain.Culture;
import sn.sunagri.production.domain.StatutCulture;
import sn.sunagri.shared.domain.TypeCulture;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Cultures", description = "Cultures, semis et récoltes")
public class CultureController {

    private final CultureService service;
    private final CurrentUserService utilisateurs;

    public CultureController(CultureService service, CurrentUserService utilisateurs) {
        this.service = service;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping("/cultures")
    public ResponseEntity<CultureResponse> declarer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @Valid @RequestBody DeclarerCultureRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Culture culture = service.declarer(utilisateur, requete.parcelleId(), requete.campagneId(),
                requete.typeCulture(), requete.superficieEmblavee());
        return ResponseEntity.created(URI.create("/api/v1/cultures/" + culture.id()))
                .body(CultureResponse.depuis(culture));
    }

    @PostMapping("/cultures/{cultureId}/semis")
    public CultureResponse semer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID cultureId,
            @Valid @RequestBody SemisRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return CultureResponse.depuis(service.semer(utilisateur, cultureId, requete.dateSemis()));
    }

    @PostMapping("/cultures/{cultureId}/recolte")
    public CultureResponse recolter(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID cultureId,
            @Valid @RequestBody RecolteRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return CultureResponse.depuis(
                service.recolter(utilisateur, cultureId, requete.dateRecolte(), requete.quantiteKg()));
    }

    @GetMapping("/cultures/{cultureId}")
    public CultureResponse detail(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID cultureId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return CultureResponse.depuis(service.detail(utilisateur, cultureId));
    }

    @GetMapping("/producteurs/{producteurId}/cultures")
    public List<CultureResponse> parProducteur(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID producteurId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return service.parProducteur(utilisateur, producteurId).stream()
                .map(CultureResponse::depuis)
                .toList();
    }

    public record DeclarerCultureRequest(
            @NotNull(message = "La parcelle est obligatoire") UUID parcelleId,
            @NotNull(message = "La campagne est obligatoire") UUID campagneId,
            @NotNull(message = "Le type de culture est obligatoire") TypeCulture typeCulture,
            @NotNull(message = "La superficie emblavée est obligatoire")
            @DecimalMin(value = "0.01", message = "La superficie emblavée doit être strictement positive")
            BigDecimal superficieEmblavee) {
    }

    public record SemisRequest(
            @NotNull(message = "La date de semis est obligatoire") LocalDate dateSemis) {
    }

    public record RecolteRequest(
            @NotNull(message = "La date de récolte est obligatoire") LocalDate dateRecolte,
            @NotNull(message = "La quantité récoltée est obligatoire")
            @DecimalMin(value = "0.01", message = "La quantité récoltée doit être strictement positive")
            BigDecimal quantiteKg) {
    }

    public record CultureResponse(
            UUID id,
            UUID parcelleId,
            UUID producteurId,
            UUID campagneId,
            TypeCulture typeCulture,
            BigDecimal superficieEmblavee,
            StatutCulture statut,
            LocalDate dateSemis,
            LocalDate dateRecolte,
            BigDecimal quantiteRecolteeKg,
            BigDecimal rendementReferenceKgHa,
            BigDecimal humiditeMax,
            int cycleMinJours) {

        static CultureResponse depuis(Culture culture) {
            return new CultureResponse(
                    culture.id(),
                    culture.parcelleId(),
                    culture.producteurId(),
                    culture.campagneId(),
                    culture.type(),
                    culture.superficieEmblavee(),
                    culture.statut(),
                    culture.dateSemis(),
                    culture.dateRecolte(),
                    culture.quantiteRecoltee(),
                    culture.parametres().rendementReferenceKgHa(),
                    culture.parametres().humiditeMax(),
                    culture.parametres().cycleMinJours());
        }
    }
}
