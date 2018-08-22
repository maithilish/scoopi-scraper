package org.codetab.scoopi.step.parse.jsoup;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSoupSelector {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupSelector.class);

    private Map<Integer, Elements> regionCache = new HashMap<>();

    public Elements selectRegion(final Document page, final String selector) {
        // regional nodes are cached for performance
        Integer hash = selector.hashCode();
        Elements elements = regionCache.get(hash);
        if (isNull(elements)) {
            elements = page.select(selector);
            regionCache.put(hash, elements);
        }
        return elements;
    }

    public String selectField(final Elements elements, final String selector,
            final String attr) {
        Elements subElements = elements.select(selector);

        String value = null;
        for (Element element : subElements) {
            if (StringUtils.isBlank(attr)) {
                value = element.ownText();
            } else {
                value = element.attr(attr); // get value by attribute key
            }
        }
        return value;
    }
}
