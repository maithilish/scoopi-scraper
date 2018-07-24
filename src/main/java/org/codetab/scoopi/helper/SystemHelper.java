package org.codetab.scoopi.helper;

import java.io.Console;

public class SystemHelper {

    public void gc() {
        System.gc();
    }

    public void printToConsole(final String format, final Object... args) {
        Console c = System.console();
        c.printf(format, args);
    }

    public String readLine() {
        Console c = System.console();
        return c.readLine();
    }
}
