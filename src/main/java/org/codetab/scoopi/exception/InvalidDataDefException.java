package org.codetab.scoopi.exception;

import org.codetab.scoopi.util.Util;

/**
 * <p>
 * Exception thrown on DataDef validation.
 * <p>
 * Checked exception : recoverable
 */
public class InvalidDataDefException extends Exception {

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
    public InvalidDataDefException(final String message) {
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
    public InvalidDataDefException(final String message,
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
    public InvalidDataDefException(final Throwable cause) {
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
