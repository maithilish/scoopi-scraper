package org.codetab.scoopi.defs;

import java.util.Collection;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;

public interface IDef {

    void init();

    void initDefProviders() throws DefNotFoundException, InvalidDefException;

    void setDefsFiles(Collection<String> defsFiles);
}
