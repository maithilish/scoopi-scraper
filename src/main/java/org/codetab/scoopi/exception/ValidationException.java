package org.codetab.scoopi.exception;

/**
 * <p>
 * Exception thrown when field is not found.
 * <p>
 * checked exception : recoverable
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * message or xpath.
     */
    private final String message;

    /**
     * <p>
     * Constructor.
     * @param message
     *            config key
     */
    public ValidationException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "[" + message + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
