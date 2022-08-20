package org.codetab.scoopi.step.parse.htmlunit;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.step.base.BaseParser;

import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Parser extends BaseParser {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ValueParser htmlUnitValueParser;
    @Inject
    private Errors errors;
    @Inject
    private Factory htmlUnitFactory;

    private static final int TIMEOUT_MILLIS = 120000;

    // TODO check behaviour if jsoup def but defaultSteps htmlUnit
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
            HtmlPage page = htmlUnitFactory.createPage(response,
                    webClient.getCurrentWindow());

            htmlUnitValueParser.setPage(page);
            setValueParser(htmlUnitValueParser);
            return true;
        } catch (IllegalStateException | IOException | DataFormatException e) {
            String message = "unable to initialize parser";
            throw new StepRunException(message, e);
        } finally {
            if (webClient != null) {
                webClient.setRefreshHandler(new ImmediateRefreshHandler());
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
            errors.inc();
            LOG.error("config: {}, use default: {} [{}]", timeout,
                    ERROR.INTERNAL, e);
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
}
