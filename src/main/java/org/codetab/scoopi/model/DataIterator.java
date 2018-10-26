package org.codetab.scoopi.model;

import static java.util.Objects.nonNull;

import java.util.Iterator;
import java.util.Stack;

public class DataIterator implements Iterator<DataComponent> {

    private Stack<Iterator<DataComponent>> stack = new Stack<>();

    private Iterator<DataComponent> currentIterator;

    public DataIterator(final Iterator<DataComponent> iterator) {
        stack.push(iterator);
    }

    @Override
    public boolean hasNext() {
        if (stack.empty()) {
            return false;
        } else {
            Iterator<DataComponent> nextIterator = stack.peek();
            if (nextIterator.hasNext()) {
                return true;
            } else {
                stack.pop();
                return hasNext();
            }
        }
    }

    @Override
    public DataComponent next() {
        if (hasNext()) {
            Iterator<DataComponent> iterator = stack.peek();
            currentIterator = iterator;
            DataComponent dc = iterator.next();
            if (dc instanceof Data) {
                stack.push(dc.iterator());
            }
            return dc;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        if (nonNull(currentIterator)) {
            currentIterator.remove();
        }
    }

}
