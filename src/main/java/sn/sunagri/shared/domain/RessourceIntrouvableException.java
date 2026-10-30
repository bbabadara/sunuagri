package sn.sunagri.shared.domain;

public class RessourceIntrouvableException extends DomaineException {

    public RessourceIntrouvableException(String message) {
        super("RESSOURCE_INTROUVABLE", message);
    }
}
