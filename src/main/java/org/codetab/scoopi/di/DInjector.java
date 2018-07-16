package org.codetab.scoopi.di;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Singleton
public class DInjector {

    private Injector injector;

    public DInjector() {
        injector = Guice.createInjector(new BasicModule());
    }

    @Inject
    public DInjector(final Injector injector) {
        this.injector = injector;
    }

    public <T> T instance(final Class<T> clz) {
        return injector.getInstance(clz);
    }
}
