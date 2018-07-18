package org.codetab.scoopi.dao.jdo;

import java.text.ParseException;
import java.util.Date;

import javax.jdo.AttributeConverter;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

public class StringDateConverter implements AttributeConverter<String, Date> {

    private final String pattern = "YYYY-MM-dd"; //$NON-NLS-1$

    @Override
    public Date convertToDatastore(final String attributeValue) {
        try {
            return DateUtils.parseDate(attributeValue, pattern);
        } catch (ParseException e) {
            // TODO log
        }
        return null;
    }

    @Override
    public String convertToAttribute(final Date datastoreValue) {
        return DateFormatUtils.format(datastoreValue, pattern);
    }

}
