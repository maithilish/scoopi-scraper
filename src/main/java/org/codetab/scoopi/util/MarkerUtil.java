package org.codetab.scoopi.util;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.messages.Messages;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>
 * Provides log marker for trace log.
 * @author Maithilish
 *
 */
public final class MarkerUtil {

    /**
     * <p>
     * Private constructor.
     */
    private MarkerUtil() {
    }

    /**
     * <p>
     * Get marker for name-group.
     * @param name
     *            name, not null
     * @param group
     *            group, not null
     * @return marker
     */
    public static Marker getMarker(final String name, final String group) {
        // without task
        return getMarker(name, group, null);
    }

    /**
     * <p>
     * Get marker for name-group-datadef.
     * @param name
     *            name, not null
     * @param group
     *            group, not null
     * @param task
     *            datadef name
     * @return marker
     */
    public static Marker getMarker(final String name, final String group,
            final String task) {

        Validate.notNull(name, Messages.getString("MarkerUtil.0")); //$NON-NLS-1$
        Validate.notNull(group, Messages.getString("MarkerUtil.1")); //$NON-NLS-1$

        String markerName = String.join("_", "LOG", name.toUpperCase(), //$NON-NLS-1$ //$NON-NLS-2$
                group.toUpperCase());
        if (task != null) {
            markerName = String.join("_", markerName, task.toUpperCase()); //$NON-NLS-1$
        }
        return MarkerFactory.getMarker(markerName);
    }

    /**
     * <p>
     * Get marker for datadef.
     * @param dataDefName
     *            datadef name, not null
     * @return marker
     */
    public static Marker getMarker(final String dataDefName) {

        Validate.notNull(dataDefName, Messages.getString("MarkerUtil.2")); //$NON-NLS-1$

        String markerName = String.join("_", "LOG", dataDefName.toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
        return MarkerFactory.getMarker(markerName);
    }
}
