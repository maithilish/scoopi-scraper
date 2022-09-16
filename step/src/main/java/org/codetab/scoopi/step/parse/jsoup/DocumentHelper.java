package org.codetab.scoopi.step.parse.jsoup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DocumentHelper {

    public Document createDocument(final InputStream html) throws IOException {
        return Jsoup.parse(html, null, "");
    }

    public InputStream getDocumentHTML(
            final org.codetab.scoopi.model.Document document)
            throws DataFormatException, IOException {
        byte[] bytes = (byte[]) document.getDocumentObject();
        return new ByteArrayInputStream(bytes);
    }
}
