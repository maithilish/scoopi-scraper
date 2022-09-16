package org.codetab.scoopi.step.parse.jsoup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.model.TaskInfo;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ValueParserTest {
    @InjectMocks
    private ValueParser valueParser;

    @Mock
    private NodeSelector nodeSelector;
    @Mock
    private TaskInfo taskInfo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testParseValue() {

        Elements blockNode = Mockito.mock(Elements.class);
        String selector = "Bar";
        String attribute = "Baz";
        String value = "Qux";
        Marker marker = Mockito.mock(Marker.class);
        String apple = "Quux";

        Document page = Mockito.mock(Document.class);
        valueParser.setPage(page);

        Map<String, String> queries = new HashMap<>();
        String blockSelector = "Foo";
        queries.put("block", blockSelector);
        queries.put("selector", selector);
        queries.put("attribute", attribute);

        when(nodeSelector.selectBlock(page, blockSelector))
                .thenReturn(blockNode);
        when(nodeSelector.selectSelector(blockNode, selector, attribute))
                .thenReturn(value);
        when(taskInfo.getMarker()).thenReturn(marker);
        when(taskInfo.getLabel()).thenReturn(apple);

        String actual = valueParser.parseValue(queries);

        assertEquals(value, actual);
    }

}
