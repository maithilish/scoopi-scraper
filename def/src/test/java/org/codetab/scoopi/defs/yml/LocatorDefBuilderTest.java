package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.util.Lists;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class LocatorDefBuilderTest {

    @InjectMocks
    private LocatorDefBuilder locatorDefBuilder;

    @Mock
    private LocatorDefs locatorDefs;

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testBuildData() throws DefNotFoundException {
        JsonNode defs = new TextNode("foo");

        LocatorGroup locatorGroup = of.createLocatorGroup("tgroup");
        List<String> groupNames = Lists.newArrayList("foo", "bar");
        List<LocatorGroup> locatorGroups = Lists.newArrayList(locatorGroup);

        when(locatorDefs.getGroupNames(defs)).thenReturn(groupNames);
        when(locatorDefs.getLocatorGroups(defs)).thenReturn(locatorGroups);

        LocatorDefData actual =
                (LocatorDefData) locatorDefBuilder.buildData(defs);

        assertThat(actual.getGroupNames()).isEqualTo(groupNames);
        assertThat(actual.getLocatorGroups()).isEqualTo(locatorGroups);
    }

}
