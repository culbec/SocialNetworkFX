package ro.ubbcluj.map.socialnetworkfx.exception;

/**
 * Exception for the Service class.
 * Should point errors in the Service class logic.
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String errorMessage, Throwable err) {
        super("ServiceException: " + errorMessage, err);
    }

    public ServiceException(String errorMessage) {
        super("ServiceException: " + errorMessage);
    }
}
