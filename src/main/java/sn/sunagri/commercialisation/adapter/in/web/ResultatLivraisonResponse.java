package sn.sunagri.commercialisation.adapter.in.web;

import java.math.BigDecimal;
import java.util.List;
import sn.sunagri.commercialisation.application.ResultatLivraison;

public record ResultatLivraisonResponse(
        CommandeResponse commande,
        BigDecimal commissionXof,
        List<PaiementResponse> paiements) {

    static ResultatLivraisonResponse depuis(ResultatLivraison resultat) {
        return new ResultatLivraisonResponse(
                CommandeResponse.depuis(resultat.commande()),
                resultat.commissionXof(),
                resultat.paiements().stream().map(PaiementResponse::depuis).toList());
    }
}
