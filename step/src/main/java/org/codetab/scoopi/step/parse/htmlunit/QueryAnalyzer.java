package org.codetab.scoopi.step.parse.htmlunit;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.step.base.BaseQueryAnalyzer;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class QueryAnalyzer extends BaseQueryAnalyzer {

    private static final Logger LOG = LogManager.getLogger();

    private static final int TIMEOUT_MILLIS = 120000;

    @Inject
    private Factory htmlUnitFactory;

    private HtmlPage page;

    @Override
    protected boolean postInitialize() {
        notNull(document, "document must not be null");
        validState(nonNull(document.getDocumentObject()),
                "documentObject is not loaded");

        WebClient webClient = null;
        try {
            String html = getDocumentHTML();
            URL url = getDocumentURL();
            StringWebResponse response =
                    htmlUnitFactory.createStringWebResponse(html, url);
            webClient = getWebClient();
            page = htmlUnitFactory.createPage(response,
                    webClient.getCurrentWindow());

            return true;
        } catch (IllegalStateException | IOException | DataFormatException e) {
            String message = "unable to initialize parser";
            throw new StepRunException(message, e);
        } finally {
            if (webClient != null) {
                webClient.setRefreshHandler(
                        htmlUnitFactory.createImmediateRefreshHandler());
                webClient.close();
            }
        }
    }

    private String getDocumentHTML() throws DataFormatException, IOException {
        byte[] bytes = (byte[]) document.getDocumentObject();
        return new String(bytes);
    }

    private WebClient getWebClient() {
        int timeout = TIMEOUT_MILLIS;
        String key = "scoopi.webClient.timeout"; //$NON-NLS-1$
        try {
            timeout = Integer.parseInt(configs.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
        }

        WebClient webClient = htmlUnitFactory.createWebClient();
        webClient.setRefreshHandler(
                htmlUnitFactory.createThreadedRefreshHandler());

        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setTimeout(timeout);
        return webClient;
    }

    private URL getDocumentURL() throws MalformedURLException {
        URL url;
        if (UrlValidator.getInstance().isValid(document.getUrl())) {
            url = htmlUnitFactory.createUrl(document.getUrl());
        } else {
            url = htmlUnitFactory.createURL(htmlUnitFactory.createUrl("file:"),
                    document.getUrl());
        }
        return url;
    }

    @Override
    protected List<String> getQueryElements(final String xpath) {
        List<String> list = new ArrayList<>();
        try {
            List<Object> elements = page.getByXPath(xpath);
            elements.stream().forEach(e -> list.add(((DomNode) e).asXml()));
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return list;
    }

    @Override
    protected String getPageSource() {
        byte[] bytes = (byte[]) document.getDocumentObject();
        return new String(bytes);
    }
}
