package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

public class DocumentTest {

    // enhanced class to test excludes in hashcode and equals
    class Enhanced extends Document {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    private Document document;

    @Before
    public void setUp() throws Exception {
        document = new Document();
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected =
                new ToStringBuilder(t1, ToStringStyle.MULTI_LINE_STYLE)
                        .append("id", t1.getId()).append("name", t1.getName())
                        .append("fromDate", t1.getFromDate())
                        .append("toDate", t1.getToDate())
                        .append("url", t1.getUrl()).toString();
        assertThat(t1.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetDocumentObject() {
        document.setDocumentObject("x");
        assertThat(document.getDocumentObject()).isEqualTo("x");
    }

    @Test
    public void testGetFromDate() {
        Date date = new Date();
        document.setFromDate(date);
        assertThat(document.getFromDate()).isEqualTo(date);
    }

    @Test
    public void testGetToDate() {
        Date date = new Date();
        document.setToDate(date);
        assertThat(document.getToDate()).isEqualTo(date);
    }

    @Test
    public void testGetUrl() {
        document.setUrl("x");
        assertThat(document.getUrl()).isEqualTo("x");
    }

    private List<Enhanced> createTestObjects() {
        Date fromDate = new Date();
        Date toDate = DateUtils.addMonths(fromDate, 1);

        Enhanced t1 = new Enhanced();
        t1.setId(1L);
        t1.setName("x");
        t1.setUrl("u");
        t1.setFromDate(fromDate);
        t1.setToDate(toDate);
        t1.setDocumentObject("d");

        Enhanced t2 = new Enhanced();
        t2.setId(2L);
        t2.setName("x");
        t2.setUrl("u");
        t2.setFromDate(fromDate);
        t2.setToDate(toDate);
        t2.setDocumentObject("d");
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
