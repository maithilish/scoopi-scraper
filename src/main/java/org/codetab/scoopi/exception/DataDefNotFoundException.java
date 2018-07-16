package org.codetab.scoopi.exception;

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
        return "[" + name + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
