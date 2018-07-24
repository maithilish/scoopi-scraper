package org.codetab.scoopi.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <p>
 * ORM tests.
 * @author Maithilish
 *
 */
public class ORMTest {

    @Test
    public void test() {
        assertEquals(ORM.JDO, ORM.values()[0]);
        assertEquals(ORM.JPA, ORM.values()[1]);
        assertEquals(ORM.DEFUALT, ORM.values()[2]);

        assertEquals(ORM.JDO, ORM.valueOf("JDO"));
        assertEquals(ORM.JPA, ORM.valueOf("JPA"));
        assertEquals(ORM.DEFUALT, ORM.valueOf("DEFUALT"));
    }
}
