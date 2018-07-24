package org.codetab.scoopi.exception;

import org.codetab.scoopi.util.Util;

/**
 * <p>
 * Exception thrown when by step when persistence error is encountered.
 * <p>
 * RuntimeException : unrecoverable
 */
public class StepPersistenceException extends RuntimeException {

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
    public StepPersistenceException(final String message) {
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
    public StepPersistenceException(final String message,
            final Throwable cause) {
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
    public StepPersistenceException(final Throwable cause) {
        super(cause);
        this.message = null;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            return message;
        } else {
            return Util.join("[", message, "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
