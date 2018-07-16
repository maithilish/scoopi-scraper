package org.codetab.scoopi;

import javax.inject.Inject;

import org.codetab.scoopi.di.DInjector;

public final class Scoopi {

    private ScoopiEngine scoopiEngine;

    @Inject
    public Scoopi(final ScoopiEngine scoopiEngine) {
        this.scoopiEngine = scoopiEngine;
    }

    public void start() {
        scoopiEngine.start();
    }

    public static void main(final String[] args) {
        DInjector dInjector = new DInjector().instance(DInjector.class);

        Scoopi scoopi = dInjector.instance(Scoopi.class);
        scoopi.start();
    }

}
