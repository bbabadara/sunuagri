package sn.sunagri.commercialisation.application;

import java.math.BigDecimal;
import java.util.List;
import sn.sunagri.commercialisation.domain.Commande;
import sn.sunagri.commercialisation.domain.PaiementDu;

public record ResultatLivraison(Commande commande, BigDecimal commissionXof, List<PaiementDu> paiements) {
}
