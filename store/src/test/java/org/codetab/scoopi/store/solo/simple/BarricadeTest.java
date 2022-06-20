package org.codetab.scoopi.store.solo.simple;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class BarricadeTest {
    @InjectMocks
    private Barricade barricade;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetup() {
        String name = "Foo";
        barricade.setup(name);
    }

    @Test
    public void testAwait() {
        barricade.await();
    }

    @Test
    public void testRelease() {
        barricade.release();
    }

    @Test
    public void testIsAllowed() {

        boolean actual = barricade.isAllowed();

        assertTrue(actual);
    }

    @Test
    public void testIsReleased() {

        boolean actual = barricade.isReleased();

        assertTrue(actual);
    }
}
