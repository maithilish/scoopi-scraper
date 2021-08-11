package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.util.Lists;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;

public class LocatorDefsTest {

    @InjectMocks
    private LocatorDefs locatorDefs;
    @Mock
    private Jacksons jacksons;

    @Mock
    private ObjectFactory objectFactory;

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testGetLocatorGroups() throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        String groupName = "foo";
        JsonNode jGroup = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode jLocators = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        JsonNode jLocator = Mockito.mock(JsonNode.class, RETURNS_DEEP_STUBS);
        Map<String, JsonNode> map = new HashMap<>();
        map.put(groupName, jGroup);
        Iterator<Entry<String, JsonNode>> entries = map.entrySet().iterator();

        String name = "tname";
        String url = "turl";
        Locator locator = of.createLocator(name, groupName, url);
        LocatorGroup locatorGroup = of.createLocatorGroup(groupName);
        locatorGroup.getLocators().add(locator);

        when(defs.fields()).thenReturn(entries);
        when(objectFactory.createLocatorGroup(groupName))
                .thenReturn(locatorGroup);
        when(jGroup.path("locators")).thenReturn(jLocators);
        when(objectFactory.createLocator(name, groupName, url))
                .thenReturn(locator);
        when(jLocators.size()).thenReturn(1);
        when(jLocators.get(1)).thenReturn(jLocator);
        when(jLocator.get("name").asText()).thenReturn(name);
        when(jLocator.get("url").asText()).thenReturn(url);

        List<LocatorGroup> actual = locatorDefs.getLocatorGroups(defs);

        assertThat(actual).hasSize(1);

        LocatorGroup aLocatorGroup = actual.get(0);

        assertThat(aLocatorGroup).isSameAs(aLocatorGroup);
        assertThat(aLocatorGroup).isEqualTo(locatorGroup);
        assertThat(aLocatorGroup.getLocators())
                .containsExactlyElementsOf(locatorGroup.getLocators());
    }

    @Test
    public void testGetLocatorGroupsEmpty() throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);

        Map<String, JsonNode> map = new HashMap<>();
        Iterator<Entry<String, JsonNode>> entries = map.entrySet().iterator();

        when(defs.fields()).thenReturn(entries);

        assertThrows(DefNotFoundException.class,
                () -> locatorDefs.getLocatorGroups(defs));
    }

    @Test
    public void testGetGroupNames() throws DefNotFoundException {
        JsonNode defs = Mockito.mock(JsonNode.class);
        List<String> groupNames = Lists.newArrayList("foo");

        when(jacksons.getFieldNames(defs)).thenReturn(groupNames)
                .thenReturn(Lists.newArrayList());

        List<String> actual = locatorDefs.getGroupNames(defs);

        assertThat(actual).isEqualTo(groupNames);

        // empty list
        assertThrows(DefNotFoundException.class,
                () -> locatorDefs.getGroupNames(defs));
    }

}
