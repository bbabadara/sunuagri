package sn.sunagri.shared.domain;

public class RegleMetierException extends DomaineException {

    public RegleMetierException(String message) {
        super("REGLE_METIER", message);
    }

    public RegleMetierException(String code, String message) {
        super(code, message);
    }
}
