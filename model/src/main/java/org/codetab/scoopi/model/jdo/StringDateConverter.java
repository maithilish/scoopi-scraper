package org.codetab.scoopi.model.jdo;

import java.text.ParseException;
import java.util.Date;

import javax.jdo.AttributeConverter;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringDateConverter implements AttributeConverter<String, Date> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StringDateConverter.class);

    private final String pattern = "YYYY-MM-dd"; //$NON-NLS-1$

    @Override
    public Date convertToDatastore(final String attributeValue) {
        try {
            return DateUtils.parseDate(attributeValue, pattern);
        } catch (ParseException e) {
            LOGGER.warn("{}", e.getMessage());
            LOGGER.debug("{}", e);
        }
        return null;
    }

    @Override
    public String convertToAttribute(final Date datastoreValue) {
        return DateFormatUtils.format(datastoreValue, pattern);
    }

}
