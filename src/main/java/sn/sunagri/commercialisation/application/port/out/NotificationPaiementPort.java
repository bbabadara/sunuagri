package sn.sunagri.commercialisation.application.port.out;

import sn.sunagri.commercialisation.domain.PaiementDu;
import sn.sunagri.production.application.ContactProducteur;

public interface NotificationPaiementPort {

    /**
     * @return {@code true} si la notification a été envoyée, {@code false} sinon.
     */
    boolean envoyer(PaiementDu paiement, ContactProducteur contact);
}
