package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codetab.scoopi.exception.ValidationException;
import org.codetab.scoopi.helper.IOHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class YamlsTest {

    @InjectMocks
    private Yamls yamls;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObjectMapper mapper;

    @Mock
    private IOHelper ioHelper;

    @Before()
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadYamls() throws Exception {
        String file = "foo";
        Collection<String> files = new ArrayList<>();
        files.add(file);

        // for loadYaml method call
        InputStream ymlStream = Mockito.mock(InputStream.class);
        JsonNode node = Mockito.mock(JsonNode.class);

        when(ioHelper.getInputStream(file)).thenReturn(ymlStream);
        when(mapper.readTree(ymlStream)).thenReturn(node);

        List<JsonNode> actual = yamls.loadYamls(files);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isSameAs(node);
    }

    @Test
    public void testLoadYaml() throws Exception {
        InputStream ymlStream = Mockito.mock(InputStream.class);
        JsonNode node = Mockito.mock(JsonNode.class);
        String file = "foo";
        when(ioHelper.getInputStream(file)).thenReturn(ymlStream);
        when(mapper.readTree(ymlStream)).thenReturn(node);
        JsonNode actual = yamls.loadYaml(file);

        assertThat(actual).isSameAs(node);
    }

    @Test
    public void testMergeNodes() {
        ObjectNode existingNode = Mockito.mock(ObjectNode.class);
        JsonNode node = Mockito.mock(JsonNode.class);
        ObjectNode mergedNodes = Mockito.mock(ObjectNode.class);

        String fieldName = "foo";
        Iterator<String> fieldNames =
                Collections.singletonList(fieldName).iterator();

        JsonNode nodes = Mockito.mock(JsonNode.class);
        ObjectNode nodeCopy = Mockito.mock(ObjectNode.class);
        List<JsonNode> nodesList = new ArrayList<>();
        nodesList.add(nodes);

        when(existingNode.isMissingNode()).thenReturn(false);

        when(mapper.createObjectNode()).thenReturn(mergedNodes);
        when(mergedNodes.path(fieldName)).thenReturn(existingNode);
        when(node.deepCopy()).thenReturn(nodeCopy);
        when(nodes.fieldNames()).thenReturn(fieldNames);
        when(nodes.path(fieldName)).thenReturn(node);

        JsonNode actual = yamls.mergeNodes(nodesList);

        assertThat(actual).isSameAs(mergedNodes);

        // existingNode is assigned to enode
        verify(existingNode).setAll(nodeCopy);
        verify(mergedNodes, times(0)).set(fieldName, nodeCopy);
    }

    @Test
    public void testMergeNodesMissingNode() {
        ObjectNode existingNode = Mockito.mock(ObjectNode.class);
        JsonNode node = Mockito.mock(JsonNode.class);
        ObjectNode mergedNodes = Mockito.mock(ObjectNode.class);

        String fieldName = "foo";
        Iterator<String> fieldNames =
                Collections.singletonList(fieldName).iterator();

        JsonNode nodes = Mockito.mock(JsonNode.class);
        ObjectNode nodeCopy = Mockito.mock(ObjectNode.class);
        List<JsonNode> nodesList = new ArrayList<>();
        nodesList.add(nodes);

        when(existingNode.isMissingNode()).thenReturn(true);

        when(mapper.createObjectNode()).thenReturn(mergedNodes);
        when(mergedNodes.path(fieldName)).thenReturn(existingNode);
        when(node.deepCopy()).thenReturn(nodeCopy);
        when(nodes.fieldNames()).thenReturn(fieldNames);
        when(nodes.path(fieldName)).thenReturn(node);

        JsonNode actual = yamls.mergeNodes(nodesList);

        assertThat(actual).isSameAs(mergedNodes);

        // existingNode is assigned to enode
        verify(existingNode, times(0)).setAll(nodeCopy);
        verify(mergedNodes).set(fieldName, nodeCopy);
    }

    @Test
    public void testValidateSchema() throws Exception {
        JsonNode schemaNodes = Mockito.mock(JsonNode.class);
        ProcessingReport report = Mockito.mock(ProcessingReport.class);
        JsonSchema schema = Mockito.mock(JsonSchema.class);
        String schemaName = "foo";
        InputStream schemaStream = Mockito.mock(InputStream.class);
        JsonNode node = Mockito.mock(JsonNode.class);
        ObjectMapper jsonMapper = Mockito.mock(ObjectMapper.class);
        JsonSchemaFactory factory = Mockito.mock(JsonSchemaFactory.class);

        when(jsonMapper.readTree(schemaStream)).thenReturn(schemaNodes);
        when(factory.getJsonSchema(schemaNodes)).thenReturn(schema);
        when(schema.validate(node)).thenReturn(report);
        when(report.isSuccess()).thenReturn(true);

        boolean actual = yamls.validateSchema(schemaName, schemaStream, node,
                jsonMapper, factory);

        assertThat(actual).isTrue();
    }

    @Test
    public void testValidateSchemaInvalid() throws Exception {
        JsonNode schemaNodes = Mockito.mock(JsonNode.class);
        ProcessingReport report = Mockito.mock(ProcessingReport.class);
        ProcessingMessage message = Mockito.mock(ProcessingMessage.class);
        Iterator<ProcessingMessage> it =
                Collections.singletonList(message).iterator();
        JsonNode mNode = Mockito.mock(JsonNode.class);

        JsonSchema schema = Mockito.mock(JsonSchema.class);
        String schemaName = "foo";
        InputStream schemaStream = Mockito.mock(InputStream.class);
        JsonNode node = Mockito.mock(JsonNode.class);
        ObjectMapper jsonMapper = Mockito.mock(ObjectMapper.class);
        JsonSchemaFactory factory = Mockito.mock(JsonSchemaFactory.class);

        when(jsonMapper.readTree(schemaStream)).thenReturn(schemaNodes);
        when(factory.getJsonSchema(schemaNodes)).thenReturn(schema);
        when(schema.validate(node)).thenReturn(report);
        when(report.iterator()).thenReturn(it);
        when(report.isSuccess()).thenReturn(false);
        when(message.asJson()).thenReturn(mNode);

        when(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mNode))
                .thenReturn("pretty");

        assertThrows(ValidationException.class,
                () -> yamls.validateSchema(schemaName, schemaStream, node,
                        jsonMapper, factory));
    }

    @Test
    public void testPretty() throws JsonProcessingException {
        JsonNode node = Mockito.mock(JsonNode.class);

        String expected = "foo";

        when(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node))
                .thenReturn(expected);
        String actual = yamls.pretty(node);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testPrettyInvalid() throws JsonProcessingException {
        JsonNode node = Mockito.mock(JsonNode.class);

        when(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node))
                .thenThrow(JsonProcessingException.class);

        String actual = yamls.pretty(node);

        assertThat(actual).isBlank();
    }

    @Test
    public void testCreateEmptyObjectNode() {
        ObjectNode apple = Mockito.mock(ObjectNode.class);
        when(mapper.createObjectNode()).thenReturn(apple);

        ObjectNode actual = yamls.createEmptyObjectNode();

        assertThat(actual).isSameAs(apple);
    }

    @Test
    public void testCreateObjectNode() {
        ObjectNode node = Mockito.mock(ObjectNode.class);
        String name = "foo";
        JsonNode defaultStepsCopy = Mockito.mock(JsonNode.class);

        when(mapper.createObjectNode()).thenReturn(node);

        ObjectNode actual = yamls.createObjectNode(name, defaultStepsCopy);

        assertThat(actual).isSameAs(node);
        verify(node).set(name, defaultStepsCopy);
    }

    @Test
    public void testToJson() throws Exception {
        JsonNode node = Mockito.mock(JsonNode.class);
        String expected = "foo";

        when(mapper.writeValueAsString(node)).thenReturn(expected);
        String actual = yamls.toJson(node);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testToJsonNode() throws Exception {
        String json = "foo";
        JsonNode node = Mockito.mock(JsonNode.class);
        when(mapper.readTree(json)).thenReturn(node);
        JsonNode actual = yamls.toJsonNode(json);
        assertThat(actual).isSameAs(node);
    }
}
