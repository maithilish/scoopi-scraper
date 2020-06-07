package org.codetab.scoopi.store;

/**
 * IStore - Interface to local or distributed Cache
 *
 * @author m
 *
 */
public interface IStore {

    boolean put(String key, Object value);

    Object get(String key);

    boolean contains(String key);
}
