package ro.ubbcluj.map.socialnetworkfx.exception;

/**
 * Exception for the Validator class.
 * Should point errors in the Validator class logic.
 */
public class ValidatorException extends RuntimeException {
    public ValidatorException(String errorMessage) {
        super(errorMessage);
    }
}
