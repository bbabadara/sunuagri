package sn.sunagri.notification.application.port.out;

public interface SmsSender {

    void envoyer(String telephone, String message);
}
