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

    private Map<Integer, List<Object>> blockCache = new HashMap<>();

    public List<Object> selectRegion(final HtmlPage page,
            final String selector) {

        // regional nodes are cached for performance
        Integer hash = selector.hashCode();
        List<Object> elements = blockCache.get(hash);

        if (isNull(elements)) {
            elements = page.getByXPath(selector);
            blockCache.put(hash, elements);
        }

        LOGGER.trace(taskInfo.getMarker(), "[{}], block nodes: {}",
                taskInfo.getLabel(), elements.size());
        for (Object element : elements) {
            traceElement(selector, element);
        }

        return elements;
    }

    public String selectSelector(final DomNode element, final String selector) {
        String value = null;

        List<?> subElements = element.getByXPath(selector);
        LOGGER.trace(taskInfo.getMarker(), "[{}], selector nodes: {}",
                taskInfo.getLabel(), subElements.size());

        for (Object o : subElements) {
            if (o instanceof DomNode) {
                value = ((DomNode) o).getTextContent();
            }
            if (o instanceof String) {
                value = (String) o;
            }
            traceElement(selector, o);
        }
        return value;
    }

    public void traceElement(final String selector, final Object element) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        String trace = null;
        if (element instanceof DomNode) {
            DomNode node = (DomNode) element;
            trace = Util.strip(node.asXml(), outerLines);
        } else {
            trace = element.toString();
        }
        LOGGER.trace(taskInfo.getMarker(), "selector: {}{}{}{}{}", selector,
                LINE, LINE, Util.indent(trace, "  "), LINE);
    }
}
