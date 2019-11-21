package org.codetab.scoopi.model.jdo;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.jdo.StringDateConverter;
import org.junit.Before;
import org.junit.Test;

public class StringDateConverterTest {

    private StringDateConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new StringDateConverter();
    }

    @Test
    public void testConvertToDatastore() throws ParseException {
        String dateStr = "2018-01-10";
        String pattern = "YYYY-MM-dd";

        Date expected = DateUtils.parseDate(dateStr, pattern);
        Date actual = converter.convertToDatastore(dateStr);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testConvertToDatastoreInvalidDate() {
        String dateStr = "2018/01/10";

        Date actual = converter.convertToDatastore(dateStr);

        assertThat(actual).isNull();
    }

    @Test
    public void testConvertToAttribute() throws ParseException {
        String dateStr = "2018-01-10";
        String pattern = "YYYY-MM-dd";

        Date date = DateUtils.parseDate(dateStr, pattern);

        String actual = converter.convertToAttribute(date);

        assertThat(actual).isEqualTo(dateStr);
    }

}
