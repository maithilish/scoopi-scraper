package org.codetab.scoopi.step.parse.htmlunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ValueParserTest {
    @InjectMocks
    private ValueParser valueParser;

    @Mock
    private NodeSelector nodeSelector;
    @Mock
    private TaskInfo taskInfo;
    @Mock
    private HtmlPage page;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testParseValue() {
        Map<String, String> queries = new HashMap<>();
        String blockSelector = "Foo";
        List<Object> block = new ArrayList<>();
        String selectorSelector = "Bar";
        DomNode o = Mockito.mock(DomNode.class);
        String value = "Baz";
        Marker marker = Mockito.mock(Marker.class);
        String grape = "Qux";

        queries.put("block", blockSelector);
        queries.put("selector", selectorSelector);
        block.add(o);

        when(nodeSelector.selectRegion(page, blockSelector)).thenReturn(block);
        when(nodeSelector.selectSelector(o, selectorSelector))
                .thenReturn(value);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(grape);

        String actual = valueParser.parseValue(queries);

        assertEquals(value, actual);
    }

    @Test
    public void testSetPage() throws Exception {
        HtmlPage page1 = Mockito.mock(HtmlPage.class);
        valueParser.setPage(page1);

        Object actual = FieldUtils.readDeclaredField(valueParser, "page", true);

        assertSame(page1, actual);
    }
}
