package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class LocatorGroupTest {

    private LocatorGroup lg;

    @Before
    public void setUp() throws Exception {
        Locator l1 = new Locator();
        l1.setId(1L);
        l1.setGroup("g1");
        l1.setName("acme");
        l1.setUrl("url");
        lg = new LocatorGroup();
        lg.setGroup("g1");
        lg.getLocators().add(l1);
    }

    @Test
    public void testHashCode() {
        assertThat(lg.hashCode()).isEqualTo(1085571794);
    }

    @Test
    public void testGetGroup() {
        assertThat(lg.getGroup()).isEqualTo("g1");
    }

    @Test
    public void testGetLocators() {
        Locator l = new Locator();
        l.setId(12L);
        l.setGroup("g1");
        l.setName("acme");
        l.setUrl("url");
        assertThat(lg.getLocators()).containsExactly(l);
    }

    @Test
    public void testEqualsObject() {
        Locator l2 = new Locator();
        l2.setId(12L);
        l2.setGroup("g1");
        l2.setName("acme");
        l2.setUrl("url");
        LocatorGroup lg2 = new LocatorGroup();
        lg2.setGroup("g1");
        lg2.getLocators().add(l2);

        assertThat(lg).isEqualTo(lg2);
    }

    @Test
    public void testToString() {
        assertThat(lg.toString())
                .isEqualTo("LocatorGroup [group=g1, locators=1]");
    }
}
