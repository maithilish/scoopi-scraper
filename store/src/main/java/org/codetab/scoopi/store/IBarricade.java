package org.codetab.scoopi.store;

public interface IBarricade {

    void setup(String name);

    void await();

    void release();

    boolean isAllowed();

    boolean isReleased();

}
