package org.codetab.scoopi.store;

import java.util.Map;

public interface ICluster {

    boolean start();

    boolean shutdown();

    Object getInstance(); // member instance

    String getMemberId(); // node/member id

    Map<String, byte[]> getMetricsHolder();
}
