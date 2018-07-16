package org.codetab.scoopi.exception;

/**
 * <p>
 * Critical exception thrown when application cannot proceed.
 * <p>
 * RuntimeException : unrecoverable
 */
public class CriticalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * message.
     */
    private final String message;

    /**
     * cause.
     */
    @SuppressWarnings("unused")
    private final Throwable cause;

    /**
     * <p>
     * Constructor.
     * @param message
     *            message
     */
    public CriticalException(final String message) {
        super(message);
        this.message = message;
        this.cause = null;
    }

    /**
     * <p>
     * Constructor.
     * @param message
     *            message
     * @param cause
     *            cause
     */
    public CriticalException(final String message, final Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    /**
     * <p>
     * Constructor.
     * @param cause
     *            cause
     */
    public CriticalException(final Throwable cause) {
        super(cause);
        this.cause = cause;
        this.message = null;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            return message;
        } else {
            return "[" + message + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
