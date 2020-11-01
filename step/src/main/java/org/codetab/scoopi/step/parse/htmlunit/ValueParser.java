package org.codetab.scoopi.step.parse.htmlunit;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.step.parse.IValueParser;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ValueParser implements IValueParser {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private NodeSelector nodeSelector;
    @Inject
    private TaskInfo taskInfo;

    // HtmlUnit page
    private HtmlPage page;

    @Override
    public String parseValue(final Map<String, String> queries) {
        String blockSelector = queries.get("block"); //$NON-NLS-1$
        List<Object> block = nodeSelector.selectRegion(page, blockSelector);

        String selectorSelector = queries.get("selector"); //$NON-NLS-1$

        String value = null;
        for (Object o : block) {
            value = nodeSelector.selectSelector((DomNode) o, selectorSelector);
        }

        LOG.trace(taskInfo.getMarker(), "[{}], value: {}", taskInfo.getLabel(),
                value);

        return value;
    }

    public void setPage(final HtmlPage page) {
        this.page = page;
    }
}
