package org.codetab.scoopi.store;

public interface IBarricade {

    void setup(String name);

    void await();

    void finish();

    boolean isAllowed();

    boolean isFinished();

}
