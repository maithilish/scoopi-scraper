package org.codetab.scoopi.di;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@Singleton
public class DInjector {

    private Injector injector;

    public DInjector(AbstractModule module) {
        injector = Guice.createInjector(module);
    }

    @Inject
    public DInjector(final Injector injector) {
        this.injector = injector;
    }

    public <T> T instance(final Class<T> clz) {
        return injector.getInstance(clz);
    }

    public <T> T instance(final String clzName, final Class<T> clz)
            throws ClassNotFoundException {
        Class<?> clzz = Class.forName(clzName);
        Object obj = injector.getInstance(clzz);
        return clz.cast(obj);
    }
}
