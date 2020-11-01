package org.codetab.scoopi.helper;

import java.io.Console;
import java.io.IOException;

public class SystemHelper {

    public void gc() {
        System.gc();
    }

    public void printToConsole(final String format, final Object... args) {
        try {
            final Console c = System.console();
            c.printf(format, args);
        } catch (final Exception e) {
            System.out.printf(format, args);
        }
    }

    public String readLine() throws IOException {
        try {
            final Console c = System.console();
            return c.readLine();
        } catch (final Exception e) {
            return "" + System.in.read();
        }
    }
}
