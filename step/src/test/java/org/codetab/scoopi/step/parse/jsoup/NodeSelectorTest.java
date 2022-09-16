package org.codetab.scoopi.step.parse.jsoup;

import static org.codetab.scoopi.util.Util.LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.TestUtils;
import org.codetab.scoopi.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
    public void testSelectBlockIfIsNull() {
        Document page = Mockito.mock(Document.class);
        String selector = "Foo";
        Elements elements = Mockito.mock(Elements.class);
        Marker marker = Mockito.mock(Marker.class);
        String grape = "Bar";
        int orange = 1;
        Element element = Mockito.mock(Element.class);
        String mango = "Baz";
        Marker marker2 = Mockito.mock(Marker.class);
        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = Mockito.mock(Iterator.class);

        when(elements.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(element);

        when(page.select(selector)).thenReturn(elements);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(grape);
        when(elements.size()).thenReturn(orange);
        when(element.outerHtml()).thenReturn(mango);

        Elements actual = nodeSelector.selectBlock(page, selector);

        assertSame(elements, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSelectBlockElseIsNull() throws Exception {
        Document page = Mockito.mock(Document.class);
        String selector = "Foo";
        Elements elements = Mockito.mock(Elements.class);
        Marker marker = Mockito.mock(Marker.class);
        String grape = "Bar";
        int orange = 1;
        Element element = Mockito.mock(Element.class);
        String mango = "Baz";
        Marker marker2 = Mockito.mock(Marker.class);
        Iterator<Element> iterator = Mockito.mock(Iterator.class);

        Map<Integer, Elements> blockCache = (Map<Integer, Elements>) FieldUtils
                .readDeclaredField(nodeSelector, "blockCache", true);
        blockCache.put(selector.hashCode(), elements);

        when(elements.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(element);

        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(grape);
        when(elements.size()).thenReturn(orange);
        when(element.outerHtml()).thenReturn(mango);

        Elements actual = nodeSelector.selectBlock(page, selector);

        assertSame(elements, actual);
        verify(page, never()).select(selector);
    }

    @Test
    public void testSelectSelectorIfStringUtilsIsBlankIfStringUtilsIsBlank() {
        Elements elements = Mockito.mock(Elements.class);
        String selector = "Foo";
        String attribute = " ";
        Elements subElements = Mockito.mock(Elements.class);
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Baz";
        int grape = 1;
        Element element = Mockito.mock(Element.class);
        String text = "Qux";
        String value = text;
        String fig = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);
        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = Mockito.mock(Iterator.class);

        when(subElements.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(element);

        when(elements.select(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(subElements.size()).thenReturn(grape);
        when(element.ownText()).thenReturn(text);
        when(element.outerHtml()).thenReturn(fig);

        String actual =
                nodeSelector.selectSelector(elements, selector, attribute);

        assertEquals(value, actual);
        verify(element, never()).attr(attribute);
    }

    @Test
    public void testSelectSelectorIfStringUtilsIsBlankElseStringUtilsIsBlankIfStringUtilsIsNotBlank() {
        Elements elements = Mockito.mock(Elements.class);
        String selector = "Foo";
        String attribute = " "; // blank
        Elements subElements = Mockito.mock(Elements.class);
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Baz";
        int grape = 2;
        Element element = Mockito.mock(Element.class);
        String text1 = "Qux";
        String text2 = "Mux";
        String value = "Qux Mux";
        String fig = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);
        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = Mockito.mock(Iterator.class);

        // loop twice to test value isBlank else
        when(subElements.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn(element);

        when(elements.select(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(subElements.size()).thenReturn(grape);
        when(element.ownText()).thenReturn(text1).thenReturn(text2);
        when(element.outerHtml()).thenReturn(fig);

        String actual =
                nodeSelector.selectSelector(elements, selector, attribute);

        assertEquals(value, actual);
        verify(element, never()).attr(attribute);
    }

    @Test
    public void testSelectSelectorIfStringUtilsIsBlankElseStringUtilsIsBlankElseStringUtilsIsNotBlank() {
        Elements elements = Mockito.mock(Elements.class);
        String selector = "Foo";
        String attribute = " "; // blank
        Elements subElements = Mockito.mock(Elements.class);
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Baz";
        int grape = 2;
        Element element = Mockito.mock(Element.class);
        String text1 = "Qux";
        String text2 = " "; // blank on 2nd iteration
        String value = "Qux";
        String fig = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);
        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = Mockito.mock(Iterator.class);

        // loop twice to test value isBlank else
        when(subElements.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn(element);

        when(elements.select(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(subElements.size()).thenReturn(grape);
        when(element.ownText()).thenReturn(text1).thenReturn(text2);
        when(element.outerHtml()).thenReturn(fig);

        String actual =
                nodeSelector.selectSelector(elements, selector, attribute);

        assertEquals(value, actual);
        verify(element, never()).attr(attribute);
    }

    @Test
    public void testSelectSelectorElseStringUtilsIsBlank() {
        Elements elements = Mockito.mock(Elements.class);
        String selector = "Foo";
        String attribute = "Bar"; // isBlank else
        Elements subElements = Mockito.mock(Elements.class);
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Baz";
        int grape = 1;
        Element element = Mockito.mock(Element.class);
        String text1 = "Qux";
        String text2 = "Sux";
        String value = "Qux Sux";
        String fig = "Quux";
        Marker marker2 = Mockito.mock(Marker.class);
        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = Mockito.mock(Iterator.class);

        // loop twice to test value isBlank else
        when(subElements.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn(element);

        when(elements.select(selector)).thenReturn(subElements);
        when(taskInfo.getMarker()).thenReturn(marker).thenReturn(marker2);
        when(taskInfo.getLabel()).thenReturn(apple);
        when(subElements.size()).thenReturn(grape);
        when(element.attr(attribute)).thenReturn(text1).thenReturn(text2);
        when(element.outerHtml()).thenReturn(fig);

        String actual =
                nodeSelector.selectSelector(elements, selector, attribute);

        assertEquals(value, actual);
        verify(element, never()).ownText();
    }

    @Test
    public void testTraceElementIf() throws Exception {
        String selector = "Foo";
        Element element = Mockito.mock(Element.class);
        String grape = "Bar";
        Marker marker = Mockito.mock(Marker.class);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(NodeSelector.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(false);

        when(element.outerHtml()).thenReturn(grape);
        when(taskInfo.getMarker()).thenReturn(marker);

        nodeSelector.traceElement(selector, element);

        verify(log, never()).trace(marker, "selector: {}{}{}{}{}", selector,
                LINE, LINE, Util.indent(grape, "  "), LINE);
    }

    @Test
    public void testTraceElementElse() throws Exception {
        String selector = "Foo";
        Element element = Mockito.mock(Element.class);
        String grape = "Bar";
        Marker marker = Mockito.mock(Marker.class);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(NodeSelector.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(true);

        when(element.outerHtml()).thenReturn(grape);
        when(taskInfo.getMarker()).thenReturn(marker);
        nodeSelector.traceElement(selector, element);

        verify(log).trace(marker, "selector: {}{}{}{}{}", selector, LINE, LINE,
                Util.indent(grape, "  "), LINE);
    }
}
