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
import sn.sunagri.commercialisation.application.ResultatLivraison;
import sn.sunagri.commercialisation.application.service.CommandeService;
import sn.sunagri.commercialisation.application.service.PaiementService;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.StatutCommande;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;
import sn.sunagri.shared.api.PageResponse;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Commandes", description = "Commandes, livraisons et paiements")
public class CommandeController {

    private final CommandeService commandes;
    private final PaiementService paiements;
    private final CurrentUserService utilisateurs;

    public CommandeController(CommandeService commandes, PaiementService paiements,
            CurrentUserService utilisateurs) {
        this.commandes = commandes;
        this.paiements = paiements;
        this.utilisateurs = utilisateurs;
    }

    @PostMapping("/offres/{offreId}/commandes")
    public ResponseEntity<CommandeResponse> passer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID offreId,
            @Valid @RequestBody PasserCommandeRequest requete) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        Commande commande = commandes.passer(utilisateur, offreId, requete.quantiteKg());
        return ResponseEntity.created(URI.create("/api/v1/commandes/" + commande.id()))
                .body(CommandeResponse.depuis(commande));
    }

    @GetMapping("/commandes")
    public PageResponse<CommandeResponse> historique(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @RequestParam(required = false) StatutCommande statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return commandes.historique(utilisateur, statut, page, size).map(CommandeResponse::depuis);
    }

    @GetMapping("/commandes/{commandeId}")
    public CommandeResponse detail(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID commandeId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return CommandeResponse.depuis(commandes.detail(utilisateur, commandeId));
    }

    @PostMapping("/commandes/{commandeId}/annulation")
    public CommandeResponse annuler(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID commandeId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return CommandeResponse.depuis(commandes.annuler(utilisateur, commandeId));
    }

    @PostMapping("/commandes/{commandeId}/livraison")
    public ResultatLivraisonResponse livrer(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID commandeId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        ResultatLivraison resultat = commandes.livrer(utilisateur, commandeId);
        return ResultatLivraisonResponse.depuis(resultat);
    }

    @GetMapping("/commandes/{commandeId}/paiements")
    public List<PaiementResponse> paiements(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID commandeId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return paiements.parCommande(utilisateur, commandeId).stream()
                .map(PaiementResponse::depuis)
                .toList();
    }

    public record PasserCommandeRequest(
            @NotNull(message = "La quantité est obligatoire")
            @DecimalMin(value = "0.01", message = "La quantité doit être strictement positive") BigDecimal quantiteKg) {
    }
}
