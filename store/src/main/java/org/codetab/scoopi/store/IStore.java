package org.codetab.scoopi.store;

/**
 * IStore - Interface to local or distributed Cache
 *
 * @author m
 *
 */
public interface IStore {

    enum StoreStatus {
        STOPPED, INITIALIZING, STARTED
    }

    boolean put(String key, Object value);

    Object get(String key);

    StoreStatus getStatus();

    void setStatus(StoreStatus status);

    String getName();

}
