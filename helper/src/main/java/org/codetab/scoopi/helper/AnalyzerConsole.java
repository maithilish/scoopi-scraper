package org.codetab.scoopi.helper;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.FileWriter;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.util.Util;

public class AnalyzerConsole {

    private static final Logger LOG = LogManager.getLogger();

    private final String dashes = "-----";
    private int lines = Integer.parseInt("5");

    private String fileName;

    public String getInput(final InputStream in) {
        try (Scanner sc = new Scanner(new CloseShieldInputStream(in))) {
            String input = displayPrompt(sc);
            switch (input) {
            case "2":
                System.out.println("enter file name");
                fileName = sc.nextLine();
                input = "2";
                break;
            case "3":
                System.out.println(
                        "enter number [show how may lines of element]");
                lines = Integer.parseInt(sc.nextLine());
                System.out.println(
                        spaceit("number lines set to:", String.valueOf(lines)));
                input = displayPrompt(sc);
                break;
            default:
                break;
            }
            return input;
        }
    }

    public String displayPrompt(final Scanner sc) {
        System.out.println("");
        System.out.println("  -- Anaylizer Menu --");
        System.out.println("");
        System.out.println("     to quite just press enter");
        System.out.println(
                "     to view elements enter query (xpath or selector)");
        System.out.println("     1 - view page source");
        System.out.println("     2 - write page source to file");
        System.out.println(
                "     3 - set number of lines to show [while display elements]");
        System.out.println("");
        return sc.nextLine();
    }

    public void showPageSource(final String pageSource) {
        System.out.println(dashes);
        System.out.println(pageSource);
        System.out.println(dashes);
    }

    public void writePageSource(final String pageSource) {
        try (FileWriter w = new FileWriter(fileName)) {
            w.write(pageSource);
            System.out.println(spaceit("wrote page source to file:", fileName));
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public void showElements(final List<String> elements) {
        for (String html : elements) {
            System.out.println(dashes);
            System.out.println(Util.strip(html, lines));
            System.out.println(dashes);
        }
        System.out.println(
                spaceit("elements found:", String.valueOf(elements.size())));
    }
}
