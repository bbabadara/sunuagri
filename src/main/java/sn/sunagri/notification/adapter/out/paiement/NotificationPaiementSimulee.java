package sn.sunagri.notification.adapter.out.paiement;

import org.springframework.stereotype.Component;
import sn.sunagri.commercialisation.application.port.out.NotificationPaiementPort;
import sn.sunagri.commercialisation.domain.PaiementDu;
import sn.sunagri.notification.application.JournalNotifications;
import sn.sunagri.notification.application.port.out.SmsSender;
import sn.sunagri.production.application.ContactProducteur;
import sn.sunagri.shared.domain.ClockPort;

@Component
public class NotificationPaiementSimulee implements NotificationPaiementPort {

    private final SmsSender sms;
    private final JournalNotifications journal;
    private final ClockPort horloge;

    public NotificationPaiementSimulee(SmsSender sms, JournalNotifications journal, ClockPort horloge) {
        this.sms = sms;
        this.journal = journal;
        this.horloge = horloge;
    }

    @Override
    public boolean envoyer(PaiementDu paiement, ContactProducteur contact) {
        String telephone = contact == null ? null : contact.telephone();
        String message = construireMessage(paiement, contact);
        try {
            sms.envoyer(telephone, message);
            journal.succes(paiement.id(), telephone, message, horloge.aujourdHui());
            return true;
        } catch (RuntimeException echec) {
            journal.echec(paiement.id(), telephone, message, echec.getMessage(), horloge.aujourdHui());
            return false;
        }
    }

    private String construireMessage(PaiementDu paiement, ContactProducteur contact) {
        String destinataire = "producteur";
        if (contact != null) {
            String nomComplet = (contact.prenom() + " " + contact.nom()).trim();
            if (!nomComplet.isBlank()) {
                destinataire = nomComplet;
            }
        }
        return "Bonjour " + destinataire + ", un paiement de " + paiement.montantXof().toPlainString()
                + " XOF vous est dû par la coopérative SunuAgri.";
    }
}
