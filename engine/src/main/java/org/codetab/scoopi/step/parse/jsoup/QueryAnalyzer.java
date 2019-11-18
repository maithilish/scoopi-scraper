package org.codetab.scoopi.step.parse.jsoup;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.step.base.BaseQueryAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryAnalyzer extends BaseQueryAnalyzer {

    static final Logger LOGGER = LoggerFactory.getLogger(QueryAnalyzer.class);

    @Inject
    private DocumentHelper documentHelper;

    private Document page;

    @Override
    protected boolean postInitialize() {
        try {
            notNull(document, "document must not be null");
            validState(nonNull(document.getDocumentObject()),
                    "documentObject is not loaded");

            InputStream html = getDocumentHTML();

            page = Jsoup.parse(html, null, "");
            return true;
        } catch (DataFormatException | IOException | IllegalStateException
                | NullPointerException e) {
            String message = "unable to initialize parser";
            throw new StepRunException(message, e);
        }
    }

    private InputStream getDocumentHTML()
            throws DataFormatException, IOException {
        byte[] bytes = documentHelper.getDocumentObject(document);
        return new ByteArrayInputStream(bytes);
    }

    @Override
    protected List<String> getQueryElements(final String selector) {
        List<String> list = new ArrayList<>();
        try {
            Elements elements = page.select(selector);
            elements.stream().forEach(e -> list.add(e.outerHtml()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return list;
    }

    @Override
    protected String getPageSource() {
        String pageSource = "";
        try {
            byte[] bytes = documentHelper.getDocumentObject(document);
            pageSource = new String(bytes);
        } catch (DataFormatException | IOException e) {
            LOGGER.error("", e);
        }
        return pageSource;
    }
}
