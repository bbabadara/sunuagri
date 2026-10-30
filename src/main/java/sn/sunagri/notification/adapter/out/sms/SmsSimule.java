package sn.sunagri.notification.adapter.out.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sn.sunagri.notification.application.port.out.SmsSender;

/**
 * Passerelle SMS simulée pour le MVP : trace le message dans les journaux applicatifs.
 */
@Component
public class SmsSimule implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SmsSimule.class);

    @Override
    public void envoyer(String telephone, String message) {
        if (telephone == null || telephone.isBlank()) {
            throw new IllegalArgumentException("Numéro de téléphone absent.");
        }
        log.info("SMS simulé vers {} : {}", telephone, message);
    }
}
