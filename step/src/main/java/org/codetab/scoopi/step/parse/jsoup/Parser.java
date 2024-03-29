package org.codetab.scoopi.step.parse.jsoup;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.step.base.BaseParser;
import org.jsoup.nodes.Document;

public class Parser extends BaseParser {

    @Inject
    private ValueParser jsoupValueParser;
    @Inject
    private DocumentHelper documentHelper;

    @Override
    protected boolean postInitialize() {
        try {
            notNull(document, "document must not be null");
            validState(nonNull(document.getDocumentObject()),
                    "documentObject is not loaded");

            InputStream html = documentHelper.getDocumentHTML(document);
            Document page = documentHelper.createDocument(html);

            jsoupValueParser.setPage(page);
            setValueParser(jsoupValueParser);
            return true;
        } catch (DataFormatException | IOException | IllegalStateException
                | NullPointerException e) {
            String message = "unable to initialize parser";
            throw new StepRunException(message, e);
        }
    }
}
