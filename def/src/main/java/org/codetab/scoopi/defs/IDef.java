package org.codetab.scoopi.defs;

import java.util.Collection;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface IDef {

    void init();

    void initDefProviders() throws DefNotFoundException, InvalidDefException,
            JsonProcessingException;

    void setDefsFiles(Collection<String> defsFiles);

    JsonNode getDefsNode(String defsName);

}
