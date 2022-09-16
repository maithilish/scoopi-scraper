package org.codetab.scoopi.step.parse.htmlunit;

import static org.codetab.scoopi.util.Util.LINE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.TestUtils;
import org.codetab.scoopi.util.Util;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class NodeSelectorTest {
    @InjectMocks
    private NodeSelector nodeSelector;

    @Mock
    private TaskInfo taskInfo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSelectRegionIfIsNull() {
        HtmlPage page = Mockito.mock(HtmlPage.class);
        String selector = "Foo";
        List<Object> elements = new ArrayList<>();
        Marker marker = Mockito.mock(Marker.class);
        String grape = "Bar";
        DomNode element = Mockito.mock(DomNode.class);
        DomNode node = Mockito.mock(DomNode.class);
        String mango = "Baz";
        String trace = "Qux";
        Marker marker2 = Mockito.mock(Marker.class);

        elements.add(element);

        when(page.getByXPath(selector)).thenReturn(elements);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(grape);
        when(node.asXml()).thenReturn(mango);
        when(element.toString()).thenReturn(trace);
        when(taskInfo.getMarker()).thenReturn(marker2);

        List<Object> actual = nodeSelector.selectRegion(page, selector);

        assertEquals(elements, actual);
    }

    @Test
    public void testSelectRegionElseIsNull() throws Exception {
        HtmlPage page = Mockito.mock(HtmlPage.class);
        String selector = "Foo";
        List<Object> elements = new ArrayList<>();
        Marker marker = Mockito.mock(Marker.class);
        String grape = "Bar";
        DomNode element = Mockito.mock(DomNode.class);
        DomNode node = Mockito.mock(DomNode.class);
        String mango = "Baz";
        String trace = "Qux";
        Marker marker2 = Mockito.mock(Marker.class);

        @SuppressWarnings("unchecked")
        Map<Integer, List<Object>> blockCache =
                (Map<Integer, List<Object>>) FieldUtils
                        .readDeclaredField(nodeSelector, "blockCache", true);

        blockCache.put(selector.hashCode(), elements);

        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(grape);
        when(node.asXml()).thenReturn(mango);
        when(element.toString()).thenReturn(trace);
        when(taskInfo.getMarker()).thenReturn(marker2);

        List<Object> actual = nodeSelector.selectRegion(page, selector);

        assertEquals(elements, actual);
        verify(page, never()).getByXPath(selector);
    }

    @Test
    public void testSelectSelectorIfIf() {
        DomNode element = Mockito.mock(DomNode.class);
        String selector = "Foo";
        List<Object> subElements = new ArrayList<>();

        Marker marker = Mockito.mock(Marker.class);
        String apple = "Bar";
        String value = "Qux";
        DomNode node = Mockito.mock(DomNode.class);
        String mango = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);

        subElements.add(value);

        when(element.getByXPath(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(element.getTextContent()).thenReturn(value);
        when(node.asXml()).thenReturn(mango);
        when(taskInfo.getMarker()).thenReturn(marker2);

        String actual = nodeSelector.selectSelector(element, selector);

        assertEquals(value, actual);
    }

    @Test
    public void testSelectSelectorIfElse() {
        DomNode element = Mockito.mock(DomNode.class);
        String selector = "Foo";
        List<Object> subElements = new ArrayList<>();
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Bar";
        String value = "Qux";
        DomNode node = Mockito.mock(DomNode.class);
        String mango = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);

        subElements.add(element);

        when(element.getByXPath(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(element.getTextContent()).thenReturn(value);
        when(node.asXml()).thenReturn(mango);
        when(taskInfo.getMarker()).thenReturn(marker2);

        String actual = nodeSelector.selectSelector(element, selector);

        assertEquals(value, actual);
    }

    @Test
    public void testSelectSelectorElse() {
        DomNode element = Mockito.mock(DomNode.class);
        String selector = "Foo";
        List<Object> subElements = new ArrayList<>();
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Bar";

        String value = "Qux";
        DomNode node = Mockito.mock(DomNode.class);
        String mango = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);

        subElements.add(value);

        when(element.getByXPath(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(node.asXml()).thenReturn(mango);
        when(taskInfo.getMarker()).thenReturn(marker2);

        String actual = nodeSelector.selectSelector(element, selector);

        assertEquals(value, actual);
        verify((element), never()).getTextContent();
    }

    @Test
    public void testTraceElementIfIf() throws Exception {
        String selector = "Foo";
        DomNode element = Mockito.mock(DomNode.class);
        DomNode node = element;
        String grape = "Bar";
        Marker marker = Mockito.mock(Marker.class);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(NodeSelector.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(true);

        when(node.asXml()).thenReturn(grape);
        when(taskInfo.getMarker()).thenReturn(marker);

        nodeSelector.traceElement(selector, element);

        verify(log).trace(marker, "selector: {}{}{}{}{}", selector, LINE, LINE,
                Util.indent(grape, "  "), LINE);
    }

    @Test
    public void testTraceElementIfElse() {
        String selector = "Foo";
        DomNode element = Mockito.mock(DomNode.class);
        DomNode node = Mockito.mock(DomNode.class);
        String trace = "Bar";
        Marker marker = Mockito.mock(Marker.class);

        when(element.toString()).thenReturn(trace);
        when(taskInfo.getMarker()).thenReturn(marker);
        nodeSelector.traceElement(selector, element);

        verify(node, never()).asXml();
    }

    @Test
    public void testTraceElementElse() throws Exception {
        String selector = "Foo";
        DomNode element = Mockito.mock(DomNode.class);
        DomNode node = element;
        String grape = "Bar";
        Marker marker = Mockito.mock(Marker.class);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(NodeSelector.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(true);

        when(node.asXml()).thenReturn(grape);
        when(taskInfo.getMarker()).thenReturn(marker);
        nodeSelector.traceElement(selector, element);

        verify(log).trace(marker, "selector: {}{}{}{}{}", selector, LINE, LINE,
                Util.indent(grape, "  "), LINE);
    }

}
