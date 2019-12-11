package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.InitModule;
import org.codetab.scoopi.engine.ScoopiEngine;

public final class Scoopi {

    @Inject
    private ScoopiEngine engine;

    public static void main(final String[] args) {
        DInjector initInjector =
                new DInjector(new InitModule()).instance(DInjector.class);
        Bootstrap bootstrap = initInjector.instance(Bootstrap.class);
        bootstrap.init();
        bootstrap.start();

        DInjector dInjector = bootstrap.getdInjector();
        Scoopi scoopi = dInjector.instance(Scoopi.class);
        scoopi.start();
    }

    public void start() {
        engine.start();
    }
}
