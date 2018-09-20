package org.codetab.scoopi.plugin.encoder;

import javax.inject.Inject;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

public class EncoderFactory {

    @Inject
    private DInjector di;

    public IEncoder<?> createEncoder(final Plugin plugin)
            throws ClassNotFoundException, DefNotFoundException {
        IEncoder<?> encoder =
                di.instance(plugin.getClassName(), IEncoder.class);
        encoder.setPlugin(plugin);
        return encoder;
    }
}
