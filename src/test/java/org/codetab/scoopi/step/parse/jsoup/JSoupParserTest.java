package org.codetab.scoopi.step.parse.jsoup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.Date;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.step.parse.IValueParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class JSoupParserTest {

    @Spy
    private JSoupValueParser jsoupValueParser;
    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    private JSoupParser parser;

    private ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        factory = new ObjectFactory();
    }

    @Test
    public void testPostInitialize()
            throws IOException, DataFormatException, IllegalAccessException {
        Document document =
                factory.createDocument("acme", "url", new Date(), new Date());
        document.setDocumentObject("test doc");
        FieldUtils.writeField(parser, "document", document, true);

        String html = "<html><body>test html</body></html>";
        byte[] bytes = html.getBytes();

        given(documentHelper.getDocumentObject(document)).willReturn(bytes);

        boolean actual = parser.postInitialize();

        IValueParser valueParser = (IValueParser) FieldUtils.readField(parser,
                "valueParser", true);
        org.jsoup.nodes.Document page = (org.jsoup.nodes.Document) FieldUtils
                .readField(jsoupValueParser, "page", true);

        assertThat(actual).isTrue();
        assertThat(valueParser).isSameAs(jsoupValueParser);
        assertThat(page.text()).isEqualTo("test html");
    }

    @Test
    public void testPostInitializeShouldThrowException()
            throws IOException, DataFormatException, IllegalAccessException {
        Document document =
                factory.createDocument("acme", "url", new Date(), new Date());
        document.setDocumentObject("test doc");
        FieldUtils.writeField(parser, "document", document, true);

        given(documentHelper.getDocumentObject(document))
                .willThrow(IOException.class);

        testRule.expect(StepRunException.class);
        parser.postInitialize();
    }

    @Test
    public void testPostInitializeInvalidState()
            throws IOException, DataFormatException, IllegalAccessException {

        try {
            parser.postInitialize();
            fail("should throw NullPointerException");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
        }

        Document document =
                factory.createDocument("acme", "url", new Date(), new Date());
        FieldUtils.writeField(parser, "document", document, true);

        try {
            parser.postInitialize();
            fail("should throw IllegalStateException");
        } catch (StepRunException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalStateException.class);
        }
    }
}
