package org.codetab.scoopi.defs;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;

/**
 * Builds serializable def data from defs such as JsonNode
 * @author m
 *
 */
public interface IDefBuilder {

    IDefData buildData(Object defs)
            throws DefNotFoundException, InvalidDefException;

}
