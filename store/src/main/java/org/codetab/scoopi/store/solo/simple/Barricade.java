package org.codetab.scoopi.store.solo.simple;

import org.codetab.scoopi.store.IBarricade;

public class Barricade implements IBarricade {

    @Override
    public void setup(final String name) {
    }

    @Override
    public void await() {
    }

    @Override
    public void release() {
    }

    @Override
    public boolean isAllowed() {
        return true;
    }

    @Override
    public boolean isReleased() {
        return true;
    }

}
