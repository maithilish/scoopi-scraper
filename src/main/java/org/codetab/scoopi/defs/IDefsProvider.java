package org.codetab.scoopi.defs;

import java.util.Collection;

public interface IDefsProvider {

    void init();

    void initProviders();

    void setDefsFiles(Collection<String> defsFiles);
}
