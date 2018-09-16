package org.codetab.scoopi.step.parse.jsoup;

import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.parse.IValueParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSoupValueParser implements IValueParser {

    static final Logger LOGGER =
            LoggerFactory.getLogger(JSoupValueParser.class);

    @Inject
    private JSoupSelector jsoupSelector;
    @Inject
    private TaskInfo taskInfo;

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

        LOGGER.trace(taskInfo.getMarker(), "[{}], value: {}",
                taskInfo.getLabel(), value);

        return value;
    }

    public void setPage(final Document page) {
        this.page = page;
    }
}
