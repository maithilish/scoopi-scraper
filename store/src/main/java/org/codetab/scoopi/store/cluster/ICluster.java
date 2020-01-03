package org.codetab.scoopi.store.cluster;

public interface ICluster {

    boolean start();

    boolean shutdown();

    Object getInstance(); // member instance

    String getMemberId(); // node/member id
}
