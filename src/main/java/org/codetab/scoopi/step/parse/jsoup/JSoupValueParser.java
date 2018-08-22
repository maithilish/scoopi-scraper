package org.codetab.scoopi.step.parse.jsoup;

import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.step.parse.IValueParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class JSoupValueParser implements IValueParser {

    @Inject
    private JSoupSelector jsoupSelector;

    // JSoup Document
    private Document page;

    @Override
    public String parseValue(final Map<String, String> queries) {
        String regionSelector = queries.get("region"); //$NON-NLS-1$
        Elements region = jsoupSelector.selectRegion(page, regionSelector);

        String fieldSelector = queries.get("field"); //$NON-NLS-1$
        // optional attribute, only for jsoup
        String attribute = queries.get("attribute"); //$NON-NLS-1$
        String value =
                jsoupSelector.selectField(region, fieldSelector, attribute);
        return value;
    }

    public void setPage(final Document page) {
        this.page = page;
    }
}
