package org.codetab.scoopi.plugin.encoder;

import org.codetab.scoopi.exception.EncodeException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Plugin;

/**
 * <p>
 * Data Encoder interface.
 * @author Maithilish
 *
 * @param <T>
 *            input type
 * @param <U>
 *            output type
 */
public interface IEncoder<U> {

    /**
     * <p>
     * Encode Data.
     * @param data
     *            to encode
     * @return encoded output
     * @throws Exception
     *             encode error
     */
    U encode(Data data) throws EncodeException;

    /**
     * <p>
     * Set fields.
     * @param fields
     *            list of fields
     */
    void setPlugin(Plugin plugin);
}
