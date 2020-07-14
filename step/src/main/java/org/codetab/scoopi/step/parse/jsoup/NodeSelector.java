package org.codetab.scoopi.step.parse.jsoup;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NodeSelector {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private TaskInfo taskInfo;

    private final int outerLines = 5;

    private Map<Integer, Elements> blockCache = new HashMap<>();

    public Elements selectBlock(final Document page, final String selector) {

        // blocks nodes are cached for performance
        Integer hash = selector.hashCode();
        Elements elements = blockCache.get(hash);

        if (isNull(elements)) {
            elements = page.select(selector);
            blockCache.put(hash, elements);
        }

        LOG.trace(taskInfo.getMarker(), "[{}], block nodes: {}",
                taskInfo.getLabel(), elements.size());
        for (Element element : elements) {
            traceElement(selector, element);
        }

        return elements;
    }

    public String selectSelector(final Elements elements, final String selector,
            final String attribute) {
        Elements subElements = elements.select(selector);

        String value = null;

        LOG.trace(taskInfo.getMarker(), "[{}], selector nodes: {}",
                taskInfo.getLabel(), subElements.size());

        for (Element element : subElements) {
            String text = null;
            if (StringUtils.isBlank(attribute)) {
                text = element.ownText();
            } else {
                // get attribute value by its key
                text = element.attr(attribute);
            }
            if (StringUtils.isBlank(value)) {
                value = text;
            } else {
                if (StringUtils.isNotBlank(text)) {
                    value = spaceit(value, text);
                }
            }
            traceElement(selector, element);
        }
        return value;
    }

    public void traceElement(final String selector, final Element element) {
        if (!LOG.isTraceEnabled()) {
            return;
        }
        String trace = Util.strip(element.outerHtml(), outerLines);
        LOG.trace(taskInfo.getMarker(), "selector: {}{}{}{}{}", selector, LINE,
                LINE, Util.indent(trace, "  "), LINE);
    }
}
