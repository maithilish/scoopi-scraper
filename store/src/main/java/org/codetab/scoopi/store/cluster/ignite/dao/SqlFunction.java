package org.codetab.scoopi.store.cluster.ignite.dao;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlFunction<T, R> {

    R apply(T t) throws SQLException;

}
