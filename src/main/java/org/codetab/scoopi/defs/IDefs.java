package org.codetab.scoopi.defs;

import java.util.Collection;

public interface IDefs {

    void init();

    void initDefProviders();

    void setDefsFiles(Collection<String> defsFiles);
}
