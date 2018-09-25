package org.codetab.scoopi.step.parse.jsoup;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.LINE;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.util.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeSelector {

    static final Logger LOGGER = LoggerFactory.getLogger(NodeSelector.class);

    @Inject
    private TaskInfo taskInfo;

    private final int outerLines = 5;

    private Map<Integer, Elements> regionCache = new HashMap<>();

    public Elements selectRegion(final Document page, final String selector) {

        // regional nodes are cached for performance
        Integer hash = selector.hashCode();
        Elements elements = regionCache.get(hash);

        if (isNull(elements)) {
            elements = page.select(selector);
            regionCache.put(hash, elements);
        }

        LOGGER.trace(taskInfo.getMarker(), "[{}], region nodes: {}",
                taskInfo.getLabel(), elements.size());
        for (Element element : elements) {
            traceElement(selector, element);
        }

        return elements;
    }

    public String selectField(final Elements elements, final String selector,
            final String attr) {
        Elements subElements = elements.select(selector);

        String value = null;

        LOGGER.trace(taskInfo.getMarker(), "[{}], field nodes: {}",
                taskInfo.getLabel(), subElements.size());

        for (Element element : subElements) {
            if (StringUtils.isBlank(attr)) {
                value = element.ownText();
            } else {
                value = element.attr(attr); // get value by attribute key
            }
            traceElement(selector, element);
        }
        return value;
    }

    public void traceElement(final String selector, final Element element) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        String trace = Util.strip(element.outerHtml(), outerLines);
        LOGGER.trace(taskInfo.getMarker(), "selector: {}{}{}{}{}", selector,
                LINE, LINE, Util.indent(trace, "  "), LINE);
    }
}
