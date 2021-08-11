package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class DefTest {

    private static ObjectMapper mapper;

    @InjectMocks
    private Def def;

    @Mock
    private Defs defs;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        mapper = new ObjectMapper();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws Exception {
        List<String> defsFiles = Lists.newArrayList("foo", "bar");
        JsonNode definedDefs = Mockito.mock(JsonNode.class);
        JsonNode defaultSteps = Mockito.mock(JsonNode.class);
        JsonNode effectiveDefs = Mockito.mock(JsonNode.class);

        Entry<String, JsonNode> entry1 = createEntry("entry1", "1");
        Entry<String, JsonNode> entry2 = createEntry("entry2", "2");

        Iterator<Entry<String, JsonNode>> entries =
                Lists.newArrayList(entry1, entry2).iterator();

        when(defs.getDefsFiles()).thenReturn(defsFiles);
        when(defs.loadDefinedDefs(defsFiles)).thenReturn(definedDefs);
        when(defs.loadDefaultSteps()).thenReturn(defaultSteps);
        when(defs.createEffectiveDefs(definedDefs)).thenReturn(effectiveDefs);
        when(effectiveDefs.fields()).thenReturn(entries);

        def.init();

        verify(defs).validateDefinedDefs(definedDefs);
        verify(defs).validateEffectiveDefs(effectiveDefs);

        // assert via GetDefsNode()
        assertThat(def.getDefsNode("entry1")).isEqualTo(entry1.getValue());
        assertThat(def.getDefsNode("entry2")).isEqualTo(entry2.getValue());

        assertThrows(CriticalException.class, () -> def.getDefsNode("entry3"));
    }

    @Test
    public void testInitDefsFilesNotNull() throws Exception {
        List<String> defsFiles = Lists.newArrayList("foo", "bar");
        JsonNode definedDefs = Mockito.mock(JsonNode.class);
        JsonNode defaultSteps = Mockito.mock(JsonNode.class);
        JsonNode effectiveDefs = Mockito.mock(JsonNode.class);

        Entry<String, JsonNode> entry1 = createEntry("entry1", "1");
        Entry<String, JsonNode> entry2 = createEntry("entry2", "2");

        Iterator<Entry<String, JsonNode>> entries =
                Lists.newArrayList(entry1, entry2).iterator();

        // this also tests the method
        def.setDefsFiles(defsFiles);

        when(defs.loadDefinedDefs(defsFiles)).thenReturn(definedDefs);
        when(defs.loadDefaultSteps()).thenReturn(defaultSteps);
        when(defs.createEffectiveDefs(definedDefs)).thenReturn(effectiveDefs);
        when(effectiveDefs.fields()).thenReturn(entries);

        def.init();

        verify(defs).validateDefinedDefs(definedDefs);
        verify(defs).validateEffectiveDefs(effectiveDefs);

        // assert via GetDefsNode()
        assertThat(def.getDefsNode("entry1")).isEqualTo(entry1.getValue());
        assertThat(def.getDefsNode("entry2")).isEqualTo(entry2.getValue());

        assertThrows(CriticalException.class, () -> def.getDefsNode("entry3"));
    }

    @Test
    public void testInitException() throws Exception {
        List<String> defsFiles = Lists.newArrayList("foo", "bar");

        when(defs.getDefsFiles()).thenReturn(defsFiles);
        when(defs.loadDefinedDefs(defsFiles))
                .thenThrow(ConfigNotFoundException.class);

        assertThrows(CriticalException.class, () -> def.init());
    }

    private Entry<String, JsonNode> createEntry(final String key,
            final String value)
            throws JsonMappingException, JsonProcessingException {
        String json = String.format("{\"k%s\":\"v%s\"}", value, value);
        JsonNode node = mapper.readTree(json);
        return new SimpleImmutableEntry<>(key, node);
    }
}
