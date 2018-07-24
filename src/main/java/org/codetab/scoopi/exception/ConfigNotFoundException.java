package org.codetab.scoopi.exception;

import org.codetab.scoopi.util.Util;

/**
 * <p>
 * Exception thrown when config not found.
 * <p>
 * checked exception : recoverable
 */
public class ConfigNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * config key.
     */
    private final String key;

    /**
     * <p>
     * Constructor.
     * @param key
     *            config key
     */
    public ConfigNotFoundException(final String key) {
        this.key = key;
    }

    @Override
    public String getMessage() {
        return Util.join("[", key, "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
