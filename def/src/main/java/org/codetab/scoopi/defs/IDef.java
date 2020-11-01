package org.codetab.scoopi.defs;

import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;

public interface IDef {

    void init();

    void setDefsFiles(Collection<String> defsFiles);

    JsonNode getDefsNode(String defsName);

}
