package org.codetab.scoopi.exception;

/**
 * <p>
 * Exception thrown on DataDef validation.
 * <p>
 * Checked exception : recoverable
 */
public class TransactionException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * message.
     */
    private final String message;
    /**
     * exception cause.
     */
    @SuppressWarnings("unused")
    private final Throwable cause;

    /**
     * <p>
     * Constructor.
     * @param message
     *            exception message
     */
    public TransactionException(final String message) {
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
    public TransactionException(final String message, final Throwable cause) {
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
    public TransactionException(final Throwable cause) {
        super(cause);
        this.message = null;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
