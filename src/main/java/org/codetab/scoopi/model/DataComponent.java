package org.codetab.scoopi.model;

import java.util.Iterator;

public class DataComponent {

    public DataComponent copy() {
        throw new UnsupportedOperationException();
    }

    public Iterator<DataComponent> iterator() {
        return new Iterator<DataComponent>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public DataComponent next() {
                return null;
            }
        };
    }
}
