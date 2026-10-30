package sn.sunagri.commercialisation.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.sunagri.commercialisation.application.service.PaiementService;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.identite.domain.Utilisateur;

@RestController
@RequestMapping("/api/v1/paiements")
@Tag(name = "Paiements", description = "Paiements dus aux producteurs")
public class PaiementController {

    private final PaiementService service;
    private final CurrentUserService utilisateurs;

    public PaiementController(PaiementService service, CurrentUserService utilisateurs) {
        this.service = service;
        this.utilisateurs = utilisateurs;
    }

    @GetMapping
    public List<PaiementResponse> lister(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @RequestParam(required = false) UUID producteurId,
            @RequestParam(required = false) UUID commandeId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        if (commandeId != null) {
            return service.parCommande(utilisateur, commandeId).stream()
                    .map(PaiementResponse::depuis)
                    .toList();
        }
        if (producteurId != null) {
            return service.parProducteur(utilisateur, producteurId).stream()
                    .map(PaiementResponse::depuis)
                    .toList();
        }
        return service.tous(utilisateur).stream()
                .map(PaiementResponse::depuis)
                .toList();
    }

    @PostMapping("/{paiementId}/paiement")
    public PaiementResponse marquerPaye(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId,
            @PathVariable UUID paiementId) {
        Utilisateur utilisateur = utilisateurs.resoudre(utilisateurId);
        return PaiementResponse.depuis(service.marquerPaye(utilisateur, paiementId));
    }
}
