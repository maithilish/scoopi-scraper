package org.codetab.scoopi.step.parse.htmlunit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.RefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Factory {

    public HtmlPage createPage(final StringWebResponse response,
            final WebWindow currentWindow) throws IOException {
        return HTMLParser.parseHtml(response, currentWindow);
    }

    public WebClient createWebClient() {
        return new WebClient(BrowserVersion.CHROME);
    }

    public RefreshHandler createThreadedRefreshHandler() {
        return new ThreadedRefreshHandler();
    }

    public URL createUrl(final String url) throws MalformedURLException {
        return new URL(url);
    }

    public URL createURL(final URL context, final String url)
            throws MalformedURLException {
        return new URL(context, url);
    }

    public StringWebResponse createStringWebResponse(final String html,
            final URL url) {
        return new StringWebResponse(html, url);
    }

    public RefreshHandler createImmediateRefreshHandler() {
        return new ImmediateRefreshHandler();
    }

}
