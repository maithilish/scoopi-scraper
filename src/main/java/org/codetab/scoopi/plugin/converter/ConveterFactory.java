package org.codetab.scoopi.plugin.converter;

import javax.inject.Inject;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Plugin;

public class ConveterFactory {

    @Inject
    private DInjector di;

    public IConverter createConverter(final Plugin plugin)
            throws DefNotFoundException, ClassNotFoundException {
        IConverter converter =
                di.instance(plugin.getClassName(), IConverter.class);
        converter.setPlugin(plugin);
        return converter;
    }
}
