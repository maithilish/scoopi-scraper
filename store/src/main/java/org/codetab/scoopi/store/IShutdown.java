package org.codetab.scoopi.store;

import java.util.function.Function;

public interface IShutdown {

    void init();

    void setDone();

    <T> boolean tryShutdown(Function<T, Boolean> func, T t);

    void setTerminate();

    void terminate();

    void cancel();
}