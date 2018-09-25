package org.codetab.scoopi.step.parse.htmlunit;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.LINE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.model.TaskInfo;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class NodeSelector {

    static final Logger LOGGER = LoggerFactory.getLogger(NodeSelector.class);

    @Inject
    private TaskInfo taskInfo;

    private final int outerLines = 5;

    private Map<Integer, List<Object>> regionCache = new HashMap<>();

    public List<Object> selectRegion(final HtmlPage page,
            final String selector) {

        // regional nodes are cached for performance
        Integer hash = selector.hashCode();
        List<Object> elements = regionCache.get(hash);

        if (isNull(elements)) {
            elements = page.getByXPath(selector);
            regionCache.put(hash, elements);
        }

        LOGGER.trace(taskInfo.getMarker(), "[{}], region nodes: {}",
                taskInfo.getLabel(), elements.size());
        for (Object element : elements) {
            traceElement(selector, element);
        }

        return elements;
    }

    public String selectField(final DomNode element, final String selector) {
        String value = null;

        List<?> subElements = element.getByXPath(selector);
        LOGGER.trace(taskInfo.getMarker(), "[{}], field nodes: {}",
                taskInfo.getLabel(), subElements.size());

        for (Object o : subElements) {
            DomNode childNode = (DomNode) o;
            value = childNode.getTextContent();
            traceElement(selector, childNode);
        }
        return value;
    }

    public void traceElement(final String selector, final Object element) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        DomNode node = (DomNode) element;
        String trace = Util.strip(node.asXml(), outerLines);
        LOGGER.trace(taskInfo.getMarker(), "selector: {}{}{}{}{}", selector,
                LINE, LINE, Util.indent(trace, "  "), LINE);
    }
}
