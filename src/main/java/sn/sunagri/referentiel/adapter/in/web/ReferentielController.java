package sn.sunagri.referentiel.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.sunagri.identite.application.CurrentUserService;
import sn.sunagri.referentiel.application.port.out.CultureReferentielRepository;

@RestController
@RequestMapping("/api/v1/referentiel")
@Tag(name = "Référentiel", description = "Paramètres de référence des cultures")
public class ReferentielController {

    private final CurrentUserService utilisateurs;
    private final CultureReferentielRepository cultures;

    public ReferentielController(CurrentUserService utilisateurs, CultureReferentielRepository cultures) {
        this.utilisateurs = utilisateurs;
        this.cultures = cultures;
    }

    @GetMapping("/cultures")
    public List<CultureReferentielResponse> cultures(
            @RequestHeader(value = "X-User-Id", required = false) String utilisateurId) {
        utilisateurs.resoudre(utilisateurId);
        return cultures.toutes().stream()
                .map(culture -> new CultureReferentielResponse(
                        culture.type().name(),
                        culture.libelle(),
                        culture.rendementKgHa(),
                        culture.humiditeMax(),
                        culture.cycleMinJours()))
                .toList();
    }

    public record CultureReferentielResponse(
            String type,
            String libelle,
            BigDecimal rendementKgHa,
            BigDecimal humiditeMax,
            int cycleMinJours) {
    }
}
