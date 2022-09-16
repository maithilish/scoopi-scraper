package org.codetab.scoopi.step.parse.jsoup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.codetab.scoopi.model.Document;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DocumentHelperTest {
    @InjectMocks
    private DocumentHelper documentHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateDocument() throws Exception {
        InputStream html1 = IOUtils.toInputStream("<html>Foo<html>",
                Charset.defaultCharset());
        InputStream html2 = IOUtils.toInputStream("<html>Foo<html>",
                Charset.defaultCharset());

        org.jsoup.nodes.Document document = Jsoup.parse(html1, null, "");

        org.jsoup.nodes.Document actual = documentHelper.createDocument(html2);

        assertEquals(document.toString(), actual.toString());
    }

    @Test
    public void testGetDocumentHTML() throws Exception {
        Document document = Mockito.mock(Document.class);
        byte[] bytes = {'F', 'o', 'o'};

        when(document.getDocumentObject()).thenReturn(bytes);

        InputStream actual = documentHelper.getDocumentHTML(document);

        assertEquals("Foo", IOUtils.toString(actual, Charset.defaultCharset()));
    }
}
