package sn.sunagri.shared.domain;

public class AccesRefuseException extends DomaineException {

    public AccesRefuseException(String message) {
        super("ACCES_REFUSE", message);
    }
}
