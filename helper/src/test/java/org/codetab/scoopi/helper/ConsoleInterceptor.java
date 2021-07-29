package org.codetab.scoopi.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Capture Console output - System.out.println
 * @author m
 *
 */
class ConsoleInterceptor {

    public String capture(final Runnable runnable) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(bos, true);
        PrintStream oldStream = System.out;
        System.setOut(printStream);
        try {
            runnable.run();
        } finally {
            System.setOut(oldStream);
        }
        return bos.toString();
    }
}
