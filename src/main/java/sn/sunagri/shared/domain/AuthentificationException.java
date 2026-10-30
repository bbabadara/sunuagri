package sn.sunagri.shared.domain;

public class AuthentificationException extends DomaineException {

    public AuthentificationException() {
        super("NON_AUTHENTIFIE", "Identité absente ou inconnue.");
    }
}
