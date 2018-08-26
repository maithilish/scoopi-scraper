package org.codetab.scoopi.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AxisNameTest {

    @Test
    public void test() {
        assertEquals(AxisName.FACT, AxisName.values()[0]);
        assertEquals(AxisName.COL, AxisName.values()[1]);
        assertEquals(AxisName.ROW, AxisName.values()[2]);
        assertEquals(AxisName.PAGE, AxisName.values()[3]);

        assertEquals(AxisName.FACT, AxisName.valueOf("FACT"));
        assertEquals(AxisName.COL, AxisName.valueOf("COL"));
        assertEquals(AxisName.ROW, AxisName.valueOf("ROW"));
        assertEquals(AxisName.PAGE, AxisName.valueOf("PAGE"));
    }

}
