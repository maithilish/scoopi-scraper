package org.codetab.scoopi.store;

import java.util.function.Function;

public interface IShutdown {

    void init();

    void setDone();

    <T, R> void tryShutdown(Function<T, R> func, T t);

    void setTerminate();

    void tryTerminate();

}
