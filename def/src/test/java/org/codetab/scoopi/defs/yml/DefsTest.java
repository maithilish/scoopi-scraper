package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.assertj.core.util.Lists;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.ValidationException;
import org.codetab.scoopi.helper.IOHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

public class DefsTest {

    @InjectMocks
    private Defs defs;

    @Mock
    private Yamls yamls;
    @Mock
    private IOHelper ioHelper;
    @Mock
    private Configs configs;
    @Mock
    private Normalizer normalizer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetDefsFiles()
            throws ConfigNotFoundException, URISyntaxException, IOException {
        String defsDir = "foo";
        Collection<String> defsFiles = Lists.newArrayList("foo", "bar");

        when(configs.getConfig("scoopi.defs.dir")).thenReturn(defsDir);
        when(ioHelper.getFilesInDir(defsDir, new String[] {"yml", "yaml"}))
                .thenReturn(defsFiles);

        Collection<String> actual = defs.getDefsFiles();

        assertThat(actual).isSameAs(defsFiles);
    }

    @Test
    public void testGetDefsFilesNoFilesInDefsDir()
            throws ConfigNotFoundException, URISyntaxException, IOException {
        String defsDir = "foo";
        Collection<String> defsFiles = Lists.newArrayList();

        when(configs.getConfig("scoopi.defs.dir")).thenReturn(defsDir);
        when(ioHelper.getFilesInDir(defsDir, new String[] {"yml", "yaml"}))
                .thenReturn(defsFiles);

        assertThrows(IllegalStateException.class, () -> defs.getDefsFiles());
    }

    @Test
    public void testLoadDefinedDefs() throws JsonProcessingException,
            IOException, ConfigNotFoundException, URISyntaxException {
        Collection<String> defsFiles = Lists.newArrayList("foo", "bar");
        JsonNode defsNode = new TextNode("baz");

        List<JsonNode> nodesList = new ArrayList<>();
        when(yamls.loadYamls(defsFiles)).thenReturn(nodesList);
        when(yamls.mergeNodes(nodesList)).thenReturn(defsNode);

        JsonNode actual = defs.loadDefinedDefs(defsFiles);

        assertThat(actual).isSameAs(defsNode);
    }

    @Test
    public void testLoadDefaultSteps() throws ConfigNotFoundException,
            JsonProcessingException, IOException, URISyntaxException {
        String defaultStepsFile = "foo";
        JsonNode defaultSteps = new TextNode("bar");

        when(configs.getConfig("scoopi.defs.defaultStepsFile"))
                .thenReturn(defaultStepsFile);
        when(yamls.loadYaml(defaultStepsFile)).thenReturn(defaultSteps);

        JsonNode actual = defs.loadDefaultSteps();

        assertThat(actual).isSameAs(defaultSteps);
    }

    @Test
    public void testMergeDefaultSteps() {
        String stepsName = "foo";
        JsonNode stepsNode = Mockito.mock(JsonNode.class);
        JsonNode stepsNodeCopy = new TextNode("bar");

        Entry<String, JsonNode> entry =
                new SimpleImmutableEntry<>(stepsName, stepsNode);
        Iterator<Entry<String, JsonNode>> steps =
                Lists.newArrayList(entry).iterator();

        // method args
        JsonNode argDefs = Mockito.mock(ObjectNode.class);
        JsonNode defaultSteps = Mockito.mock(JsonNode.class);

        JsonNode defsSteps = Mockito.mock(ObjectNode.class);

        when(defaultSteps.at("/steps")).thenReturn(stepsNode);
        when(stepsNode.fields()).thenReturn(steps);
        when(argDefs.at("/steps")).thenReturn(defsSteps);
        when(stepsNode.deepCopy()).thenReturn(stepsNodeCopy);

        defs.mergeDefaultSteps(argDefs, defaultSteps);

        verify(((ObjectNode) defsSteps)).set(stepsName, stepsNodeCopy);
    }

    @Test
    public void testMergeDefaultStepsIsMissingNode() {
        String stepsName = "foo";
        JsonNode stepsNode = Mockito.mock(JsonNode.class);
        JsonNode stepsNodeCopy = new TextNode("bar");

        Entry<String, JsonNode> entry =
                new SimpleImmutableEntry<>(stepsName, stepsNode);
        Iterator<Entry<String, JsonNode>> steps =
                Lists.newArrayList(entry).iterator();

        // method args
        JsonNode argDefs = Mockito.mock(ObjectNode.class);
        JsonNode defaultSteps = Mockito.mock(JsonNode.class);

        JsonNode defsSteps = MissingNode.getInstance();
        ObjectNode newNode = Mockito.mock(ObjectNode.class);

        when(defaultSteps.at("/steps")).thenReturn(stepsNode);
        when(stepsNode.fields()).thenReturn(steps);
        when(argDefs.at("/steps")).thenReturn(defsSteps);
        when(stepsNode.deepCopy()).thenReturn(stepsNodeCopy);
        when(yamls.createObjectNode(stepsName, stepsNodeCopy))
                .thenReturn(newNode);

        defs.mergeDefaultSteps(argDefs, defaultSteps);

        verify((ObjectNode) argDefs).set("steps", newNode);
    }

    @Test
    public void testValidateDefinedDefs() throws ConfigNotFoundException,
            ProcessingException, IOException, ValidationException {
        String schema = "foo";
        InputStream schemaStream = new ByteArrayInputStream("baz".getBytes());
        JsonNode definedDefs = new TextNode("bar");

        when(configs.getConfig("scoopi.defs.definedSchema")).thenReturn(schema);
        when(ioHelper.getInputStream(schema)).thenReturn(schemaStream);

        defs.validateDefinedDefs(definedDefs);

        verify(yamls).validateSchema(schema, schemaStream, definedDefs);
    }

    @Test
    public void testValidateEffectiveDefs() throws ConfigNotFoundException,
            ProcessingException, IOException, ValidationException {
        String schema = "foo";
        JsonNode effectiveDefs = new TextNode("bar");
        InputStream schemaStream = new ByteArrayInputStream("baz".getBytes());

        when(configs.getConfig("scoopi.defs.effectiveSchema"))
                .thenReturn(schema);
        when(ioHelper.getInputStream(schema)).thenReturn(schemaStream);

        defs.validateEffectiveDefs(effectiveDefs);

        verify(yamls).validateSchema(schema, schemaStream, effectiveDefs);
    }

    @Test
    public void testCreateEffectiveDefs()
            throws IOException, DefNotFoundException, ConfigNotFoundException {

        String defaultStepsName = "foo";

        JsonNode argDefs = Mockito.mock(JsonNode.class);
        JsonNode eDefs = new TextNode(defaultStepsName);

        when(configs.getConfig("scoopi.defs.defaultSteps"))
                .thenReturn(defaultStepsName);
        when(argDefs.deepCopy()).thenReturn(eDefs);

        JsonNode actual = defs.createEffectiveDefs(argDefs);

        assertThat(actual).isEqualTo(eDefs);

        InOrder inOrder = inOrder(normalizer);

        inOrder.verify(normalizer).addFactsDim(eDefs);
        inOrder.verify(normalizer).addItemIndex(eDefs);
        inOrder.verify(normalizer).addItemOrder(eDefs);
        inOrder.verify(normalizer).expandSteps(eDefs);
        inOrder.verify(normalizer).setDefaultSteps(eDefs, defaultStepsName);
        inOrder.verify(normalizer).expandTaskSteps(eDefs);

        verifyNoMoreInteractions(normalizer);
    }

    @Test
    public void testCreateEffectiveDefsConfigNotFound()
            throws IOException, DefNotFoundException, ConfigNotFoundException {

        String defaultStepsName = "jsoupDefault";

        JsonNode argDefs = Mockito.mock(JsonNode.class);
        JsonNode eDefs = new TextNode(defaultStepsName);

        when(configs.getConfig("scoopi.defs.defaultSteps"))
                .thenThrow(ConfigNotFoundException.class);
        when(argDefs.deepCopy()).thenReturn(eDefs);

        JsonNode actual = defs.createEffectiveDefs(argDefs);

        assertThat(actual).isEqualTo(eDefs);

        InOrder inOrder = inOrder(normalizer);

        inOrder.verify(normalizer).addFactsDim(eDefs);
        inOrder.verify(normalizer).addItemIndex(eDefs);
        inOrder.verify(normalizer).addItemOrder(eDefs);
        inOrder.verify(normalizer).expandSteps(eDefs);
        inOrder.verify(normalizer).setDefaultSteps(eDefs, defaultStepsName);
        inOrder.verify(normalizer).expandTaskSteps(eDefs);

        verifyNoMoreInteractions(normalizer);
    }

    @Test
    public void testPretty() {
        JsonNode node = new TextNode("foo");

        when(yamls.pretty(node)).thenReturn("bar");

        assertThat(defs.pretty(node)).isEqualTo("bar");
    }
}
