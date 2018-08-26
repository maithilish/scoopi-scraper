package org.codetab.scoopi.model;

public enum AxisName {

    FACT, COL, ROW, PAGE;

    private static AxisName[] reverse = setReverse();

    private static AxisName[] setReverse() {
        int size = AxisName.values().length;
        AxisName[] rValues = new AxisName[size];
        int i = 0;
        for (int j = size - 1; j >= 0; j--) {
            rValues[i++] = AxisName.values()[j];
        }
        return rValues;
    }

    public static AxisName[] getReverseValues() {
        return reverse;
    }
}
