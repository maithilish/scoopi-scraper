package org.codetab.scoopi.step.parse.jsoup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JSoupValueParserTest {

    @Mock
    private JSoupSelector jsoupSelector;

    @InjectMocks
    private JSoupValueParser jSoupValueParser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseValue() {
        Document page = new Document("test uri");
        jSoupValueParser.setPage(page);

        String regionSelector = "test region";
        String fieldSelector = "test field";
        String attribute = "test attribute";

        String value = "test value";

        Map<String, String> queries = new HashMap<>();
        queries.put("region", regionSelector);
        queries.put("field", fieldSelector);
        queries.put("attribute", attribute);

        Elements regionElements = new Elements();

        given(jsoupSelector.selectRegion(page, regionSelector))
                .willReturn(regionElements);
        given(jsoupSelector.selectField(regionElements, fieldSelector,
                attribute)).willReturn(value);

        String actual = jSoupValueParser.parseValue(queries);

        assertThat(actual).isEqualTo(value);
    }

}
