package ro.ubbcluj.map.socialnetworkfx.exception;

/**
 * Exception for the Repository class.
 * Should point errors in the Repository class logic.
 */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String errorMessage) {
        super("RepositoryException: " + errorMessage);
    }
}
