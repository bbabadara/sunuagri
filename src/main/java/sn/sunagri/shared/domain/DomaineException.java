package sn.sunagri.shared.domain;

public abstract class DomaineException extends RuntimeException {

    private final String code;

    protected DomaineException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
