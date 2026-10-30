package sn.sunagri.entrepot.adapter.in.web;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.sunagri.entrepot.application.service.LivraisonService;
import sn.sunagri.entrepot.application.service.StockService;
import sn.sunagri.entrepot.domain.Livraison;
import sn.sunagri.entrepot.domain.StockLigne;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.domain.Qualite;
import sn.sunagri.shared.domain.TypeCulture;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Entrepôt", description = "Livraisons et stock")
public class EntrepotController {

    private final LivraisonService livraisons;
    private final StockService stock;
    private final CurrentUserService utilisateurs;

    public EntrepotController(LivraisonService livraisons, StockService stock, CurrentUserService utilisateurs) {
        this.livraisons = livraisons;
        this.stock = stock;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping("/cultures/{cultureId}/livraisons")
    public ResponseEntity<LivraisonResponse> livrer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID cultureId,
            @Valid @RequestBody LivrerRecolteRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Livraison livraison = livraisons.livrer(utilisateur, cultureId, requete.quantiteKg(),
                requete.tauxHumidite(), requete.qualite());
        return ResponseEntity.created(URI.create("/api/v1/livraisons/" + livraison.id()))
                .body(LivraisonResponse.depuis(livraison));
    }

    @GetMapping("/stock")
    public List<StockLigneResponse> stock(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @RequestParam(required = false) TypeCulture typeCulture,
            @RequestParam(required = false) Qualite qualite) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return stock.consulter(utilisateur, typeCulture, qualite).stream()
                .map(StockLigneResponse::depuis)
                .toList();
    }

    public record LivrerRecolteRequest(
            @NotNull(message = "La quantité livrée est obligatoire")
            @DecimalMin(value = "0.01", message = "La quantité livrée doit être strictement positive")
            BigDecimal quantiteKg,
            @NotNull(message = "Le taux d'humidité est obligatoire")
            @DecimalMin(value = "0", message = "Le taux d'humidité ne peut pas être négatif")
            BigDecimal tauxHumidite,
            @NotNull(message = "La qualité est obligatoire") Qualite qualite) {
    }

    public record LivraisonResponse(
            UUID id,
            UUID cultureId,
            UUID producteurId,
            TypeCulture typeCulture,
            Qualite qualite,
            BigDecimal quantiteKg,
            BigDecimal tauxHumidite,
            LocalDate dateLivraison,
            UUID lotId) {

        static LivraisonResponse depuis(Livraison livraison) {
            return new LivraisonResponse(
                    livraison.id(),
                    livraison.cultureId(),
                    livraison.producteurId(),
                    livraison.typeCulture(),
                    livraison.qualite(),
                    livraison.quantiteKg(),
                    livraison.tauxHumidite(),
                    livraison.dateLivraison(),
                    livraison.lotId());
        }
    }

    public record StockLigneResponse(
            TypeCulture typeCulture,
            Qualite qualite,
            BigDecimal totalKg,
            BigDecimal reserveKg,
            BigDecimal disponibleKg) {

        static StockLigneResponse depuis(StockLigne ligne) {
            return new StockLigneResponse(
                    ligne.typeCulture(),
                    ligne.qualite(),
                    ligne.totalKg(),
                    ligne.reserveKg(),
                    ligne.disponibleKg());
        }
    }
}
