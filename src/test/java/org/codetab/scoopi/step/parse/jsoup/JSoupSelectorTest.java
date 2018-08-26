package org.codetab.scoopi.step.parse.jsoup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JSoupSelectorTest {

    @InjectMocks
    private JSoupSelector jSoupSelector;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSelectRegion() {
        Element element1 = new Element("ele 1");
        Element element2 = new Element("ele 2");

        Elements elements = new Elements(element1, element2);

        Document page = Mockito.mock(Document.class);

        String selector = "test selector";

        given(page.select(selector)).willReturn(elements);

        Elements actual = jSoupSelector.selectRegion(page, selector);

        assertThat(actual).isEqualTo(elements);

        actual = jSoupSelector.selectRegion(page, selector);
        assertThat(actual).isEqualTo(elements);

        verify(page, times(1)).select(selector);
    }

    @Test
    public void testSelectField() {
        Elements elements = Mockito.mock(Elements.class);

        Element element1 = Mockito.mock(Element.class);
        Element element2 = Mockito.mock(Element.class);

        Elements subElements = new Elements(element1, element2);

        String selector = "test selector";
        String attribute = "test attribute";
        String value = "test value";

        given(elements.select(selector)).willReturn(subElements);
        given(element2.attr(attribute)).willReturn(value);

        String actual =
                jSoupSelector.selectField(elements, selector, attribute);

        assertThat(actual).isEqualTo(value);
    }

    @Test
    public void testSelectFieldBlankAttribute() {
        Elements elements = Mockito.mock(Elements.class);

        Element element1 = Mockito.mock(Element.class);
        Element element2 = Mockito.mock(Element.class);

        Elements subElements = new Elements(element1, element2);

        String selector = "test selector";
        String attribute = "";
        String value = "test value";

        given(elements.select(selector)).willReturn(subElements);
        given(element2.ownText()).willReturn(value);

        String actual =
                jSoupSelector.selectField(elements, selector, attribute);

        assertThat(actual).isEqualTo(value);
    }
}
