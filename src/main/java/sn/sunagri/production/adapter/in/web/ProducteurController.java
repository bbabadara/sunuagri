package sn.sunagri.production.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RestController;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.production.application.service.ProducteurService;
import sn.sunagri.production.domain.Parcelle;
import sn.sunagri.production.domain.Producteur;

@RestController
@RequestMapping("/api/v1/producteurs")
@Tag(name = "Producteurs", description = "Producteurs et parcelles")
public class ProducteurController {

    private final ProducteurService service;
    private final CurrentUserService utilisateurs;

    public ProducteurController(ProducteurService service, CurrentUserService utilisateurs) {
        this.service = service;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping
    public ResponseEntity<ProducteurResponse> enregistrer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @Valid @RequestBody EnregistrerProducteurRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Producteur producteur = service.enregistrer(utilisateur, requete.nom(), requete.prenom(),
                requete.telephone(), requete.village());
        return ResponseEntity.created(URI.create("/api/v1/producteurs/" + producteur.id()))
                .body(ProducteurResponse.depuis(producteur));
    }

    @GetMapping
    public List<ProducteurResponse> tous(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return service.tous(utilisateur).stream().map(ProducteurResponse::depuis).toList();
    }

    @GetMapping("/{producteurId}")
    public ProducteurResponse detail(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID producteurId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return ProducteurResponse.depuis(service.detail(utilisateur, producteurId));
    }

    @PostMapping("/{producteurId}/parcelles")
    public ResponseEntity<ParcelleResponse> declarerParcelle(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID producteurId,
            @Valid @RequestBody DeclarerParcelleRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Parcelle parcelle = service.declarerParcelle(utilisateur, producteurId, requete.nom(),
                requete.superficie(), requete.commune());
        return ResponseEntity.created(URI.create("/api/v1/producteurs/" + producteurId + "/parcelles/" + parcelle.id()))
                .body(ParcelleResponse.depuis(parcelle));
    }

    @GetMapping("/{producteurId}/parcelles")
    public List<ParcelleResponse> parcelles(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID producteurId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return service.parcelles(utilisateur, producteurId).stream()
                .map(ParcelleResponse::depuis)
                .toList();
    }

    @GetMapping("/{producteurId}/parcelles/{parcelleId}")
    public ParcelleResponse parcelle(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID producteurId,
            @PathVariable UUID parcelleId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return ParcelleResponse.depuis(service.parcelle(utilisateur, producteurId, parcelleId));
    }

    public record EnregistrerProducteurRequest(
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotBlank(message = "Le prénom est obligatoire") String prenom,
            @NotBlank(message = "Le téléphone est obligatoire") String telephone,
            @NotBlank(message = "Le village est obligatoire") String village) {
    }

    public record DeclarerParcelleRequest(
            @NotBlank(message = "Le nom de la parcelle est obligatoire") String nom,
            @NotNull(message = "La superficie est obligatoire")
            @DecimalMin(value = "0.01", message = "La superficie doit être strictement positive") BigDecimal superficie,
            @NotBlank(message = "La commune est obligatoire") String commune) {
    }

    public record ProducteurResponse(
            UUID id,
            String nom,
            String prenom,
            String telephone,
            String village,
            BigDecimal superficieTotaleHa,
            int nombreParcelles) {

        static ProducteurResponse depuis(Producteur producteur) {
            return new ProducteurResponse(
                    producteur.id(),
                    producteur.nom(),
                    producteur.prenom(),
                    producteur.telephone(),
                    producteur.village(),
                    producteur.superficieTotale(),
                    producteur.parcelles().size());
        }
    }

    public record ParcelleResponse(UUID id, String nom, BigDecimal superficie, String commune) {

        static ParcelleResponse depuis(Parcelle parcelle) {
            return new ParcelleResponse(parcelle.id(), parcelle.nom(), parcelle.superficie(), parcelle.commune());
        }
    }
}
