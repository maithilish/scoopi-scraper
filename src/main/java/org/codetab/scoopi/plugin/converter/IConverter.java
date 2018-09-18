package org.codetab.scoopi.plugin.converter;

import org.codetab.scoopi.model.Plugin;

/**
 * <p>
 * Converter interface.
 * @author Maithilish
 *
 * @param <T>
 *            input type
 * @param <U>
 *            output type
 */
public interface IConverter {

    /**
     * <p>
     * Convert input type to output.
     * @param input
     *            to convert
     * @return converted output
     * @throws Exception
     *             convert error
     */
    String convert(String input) throws Exception;

    void setPlugin(Plugin plugin);
}
