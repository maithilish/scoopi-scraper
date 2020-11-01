package org.codetab.scoopi.step.parse.jsoup;

import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.parse.IValueParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ValueParser implements IValueParser {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private NodeSelector nodeSelector;
    @Inject
    private TaskInfo taskInfo;

    // JSoup Document
    private Document page;

    @Override
    public String parseValue(final Map<String, String> queries) {
        String blockSelector = queries.get("block"); //$NON-NLS-1$
        Elements blockNode = nodeSelector.selectBlock(page, blockSelector);

        String selector = queries.get("selector"); //$NON-NLS-1$
        // optional attribute, only for jsoup
        String attribute = queries.get("attribute"); //$NON-NLS-1$
        String value =
                nodeSelector.selectSelector(blockNode, selector, attribute);

        LOG.trace(taskInfo.getMarker(), "[{}], value: {}", taskInfo.getLabel(),
                value);

        return value;
    }

    public void setPage(final Document page) {
        this.page = page;
    }
}
