package org.codetab.scoopi.exception;

import org.codetab.scoopi.util.Util;

/**
 * <p>
 * Exception thrown when DataDef not found.
 * <p>
 * CheckedException : recoverable
 */
public final class DataDefNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * datadef name.
     */
    private final String name;

    /**
     * <p>
     * Constructor.
     * @param name
     *            datadef name
     */
    public DataDefNotFoundException(final String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return Util.join("[", name, "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
