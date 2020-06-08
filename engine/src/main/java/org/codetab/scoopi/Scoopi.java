package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.bootstrap.ConfigsComposer;
import org.codetab.scoopi.bootstrap.DefsComposer;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.engine.ScoopiEngine;

public final class Scoopi {

    @Inject
    private ScoopiEngine scoopiEngine;

    public static void main(final String[] args) {

        // bootstrap solo or cluster DI
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.boot();
        DInjector dInjector = bootstrap.getdInjector();

        // FIXME - bootfix, only leader should boot config and def
        /**
         * Boot configs and defs. ConfigBooter has to boot before DefBooter
         * creation, so can't be injected to Scoopi.
         */
        dInjector.instance(ConfigsComposer.class).compose();
        dInjector.instance(DefsComposer.class).compose();

        // start scoopi
        Scoopi scoopi = dInjector.instance(Scoopi.class);
        scoopi.start();
    }

    public void start() {
        scoopiEngine.start();
    }
}
