package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class LocatorDefDataTest {

    private LocatorDefData locatorDefData;

    @Before
    public void setUp() throws Exception {
        locatorDefData = new LocatorDefData();
    }

    @Test
    public void testGetGroupNames() {
        List<String> groupNames = Lists.newArrayList("foo");
        locatorDefData.setGroupNames(groupNames);

        assertThat(locatorDefData.getGroupNames()).isSameAs(groupNames);
    }

    @Test
    public void testGetLocatorGroups() {
        ObjectFactory of = new ObjectFactory();
        LocatorGroup locatorGroup = of.createLocatorGroup("foo");
        List<LocatorGroup> locatorGroups = Lists.newArrayList(locatorGroup);

        locatorDefData.setLocatorGroups(locatorGroups);
        assertThat(locatorDefData.getLocatorGroups()).isSameAs(locatorGroups);
    }
}
