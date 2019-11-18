package org.codetab.scoopi.exception;

/**
 * <p>
 * Exception thrown by encoders
 * <p>
 * checked exception : recoverable
 */
public class EncodeException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String message;
    private Throwable cause;

    public EncodeException(final String message) {
        super(message);
        this.message = message;
        this.cause = null;
    }

    /**
     * <p>
     * Constructor.
     * @param message
     *            exception message
     * @param cause
     *            exception cause
     */
    public EncodeException(final String message, final Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    /**
     * <p>
     * Constructor.
     * @param cause
     *            exception cause
     */
    public EncodeException(final Throwable cause) {
        super(cause);
        this.message = null;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "EncodeException [message=" + message + ", cause=" + cause + "]";
    }
}
