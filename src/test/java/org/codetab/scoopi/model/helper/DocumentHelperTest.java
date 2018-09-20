package org.codetab.scoopi.model.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.system.ConfigService;
import org.codetab.scoopi.util.CompressionUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * <p>
 * DocumentHelper Tests.
 *
 * @author Maithilish
 *
 */
public class DocumentHelperTest {

    @Mock
    private ConfigService configService;
    @Mock
    private ObjectFactory objectFactory;

    @InjectMocks
    private DocumentHelper documentHelper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();
    private JobInfo jobInfo;
    private Document document;
    // real factory to create test objects
    private ObjectFactory factory = new ObjectFactory();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        jobInfo = factory.createJobInfo(0, "locator1", "group1", "task1",
                "steps1", "dataDef1");
        document =
                factory.createDocument("name", "url", new Date(), new Date());
    }

    @Test
    public void testGetActiveDocumentId() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};

        List<Document> documents = getTestDocuments();

        Date runDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);

        given(configService.getRunDateTime()).willReturn(runDate);

        Long actual = documentHelper.getActiveDocumentId(documents);

        assertThat(actual).isEqualTo(2L);

        runDate = DateUtils.parseDate("01-07-2017 09:59:59.999", parsePatterns);
        given(configService.getRunDateTime()).willReturn(runDate);

        actual = documentHelper.getActiveDocumentId(documents);

        assertThat(actual).isEqualTo(2L);

        runDate = DateUtils.parseDate("01-07-2017 10:00:00.001", parsePatterns);
        given(configService.getRunDateTime()).willReturn(runDate);

        actual = documentHelper.getActiveDocumentId(documents);

        assertThat(actual).isNull();
    }

    @Test
    public void testGetActiveDocumentIdDocumentsNull() throws ParseException {
        List<Document> documents = null;
        Long actual = documentHelper.getActiveDocumentId(documents);
        assertThat(actual).isNull();
    }

    @Test
    public void testGetActiveDocumentIdDocumentsEmpty() throws ParseException {
        List<Document> documents = new ArrayList<>();
        Long actual = documentHelper.getActiveDocumentId(documents);
        assertThat(actual).isNull();
    }

    @Test
    public void testgetDocument() throws ParseException {
        List<Document> documents = getTestDocuments();

        Document actual = documentHelper.getDocument(1L, documents);
        assertThat(actual).isEqualTo(documents.get(0));

        actual = documentHelper.getDocument(2L, documents);
        assertThat(actual).isEqualTo(documents.get(1));
    }

    @Test
    public void testgetDocumentShouldThrowException() throws ParseException {
        List<Document> documents = getTestDocuments();

        testRule.expect(NoSuchElementException.class);
        documentHelper.getDocument(3L, documents);
    }

    @Test
    public void testSetDatesIllegalState() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(documentHelper, "configService", null,
                true);
        try {
            documentHelper.getActiveDocumentId(new ArrayList<>());
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("configService is not set");
        }
    }

    @Test
    public void testGetToDateWithLiveField() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        String live = "P2D";

        Date expected = DateUtils.addDays(fromDate, 2);

        // when
        Date actual = documentHelper.getToDate(fromDate, live, jobInfo);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetToDateBlankLive() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        String live = "";

        // when
        Date actual = documentHelper.getToDate(fromDate, live, jobInfo);

        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateWithZeroLive() throws ParseException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate =
                DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        String live = "0";

        // when
        Date actual = documentHelper.getToDate(fromDate, live, jobInfo);
        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateWithDateString()
            throws ParseException, ConfigNotFoundException {
        Date fromDate = new Date();
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        String live = "01-08-2017 11:00:00.000";

        given(configService.getConfigArray("gotz.dateParsePattern"))
                .willReturn(parsePatterns);
        Date expected = DateUtils.parseDate(live, parsePatterns);

        // when
        Date actual = documentHelper.getToDate(fromDate, live, jobInfo);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetToDateWithInvalidDateString()
            throws ParseException, ConfigNotFoundException {
        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Date fromDate = new Date();
        String live = "01-xx-2017 11:00:00.000";

        given(configService.getConfigArray("gotz.dateParsePattern"))
                .willReturn(parsePatterns);

        // when
        Date actual = documentHelper.getToDate(fromDate, live, jobInfo);
        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateParsePatternNotFound()
            throws ParseException, ConfigNotFoundException {
        Date fromDate = new Date();
        String live = "01-xx-2017 11:00:00.000";

        given(configService.getConfigArray("gotz.dateParsePattern"))
                .willThrow(ConfigNotFoundException.class);

        // when
        Date actual = documentHelper.getToDate(fromDate, live, jobInfo);
        assertThat(actual).isEqualTo(fromDate);
    }

    @Test
    public void testGetToDateNullParams() {
        try {
            documentHelper.getToDate(null, "0", jobInfo);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fromDate must not be null");
        }

        try {
            documentHelper.getToDate(new Date(), null, jobInfo);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("live must not be null");
        }

        try {
            documentHelper.getToDate(new Date(), "", null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("jobInfo must not be null");
        }
    }

    @Test
    public void testGetToDateIllegalState() throws IllegalAccessException {
        FieldUtils.writeDeclaredField(documentHelper, "configService", null,
                true);
        try {
            documentHelper.getToDate(new Date(), "", jobInfo);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("configService is not set");
        }
    }

    @Test
    public void testSetDocumentObjectCompression() throws IOException {
        byte[] documentObject = String.valueOf("some string").getBytes();

        // when
        boolean actual =
                documentHelper.setDocumentObject(document, documentObject);

        byte[] expected =
                CompressionUtil.compressByteArray(documentObject, 1024);

        assertThat(actual).isTrue();
        assertThat(document.getDocumentObject()).isEqualTo(expected);
    }

    @Test
    public void testSetDocumentObjectNullParams() throws IOException {
        try {
            documentHelper.setDocumentObject(null, new String().getBytes());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("document must not be null");
        }

        try {
            documentHelper.setDocumentObject(document, null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("documentObject must not be null");
        }
    }

    @Test
    public void testGetDocumentObjectNullParams()
            throws DataFormatException, IOException {
        try {
            documentHelper.getDocumentObject(null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("document must not be null");
        }

        try {
            // document without documentObject
            documentHelper.getDocumentObject(document);
            fail("must throw NullPointerException");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("documentObject is null");
        }
    }

    @Test
    public void testGetDocumentObject()
            throws IOException, DataFormatException {
        byte[] documentObject = String.valueOf("some string").getBytes();
        documentHelper.setDocumentObject(document, documentObject);

        // when
        byte[] actual = documentHelper.getDocumentObject(document);

        assertThat(actual).isEqualTo(documentObject);
    }

    @Test
    public void testCreateDocument() {
        Date fromDate = document.getFromDate();
        Date toDate = document.getToDate();

        given(objectFactory.createDocument("name", "url", fromDate, toDate))
                .willReturn(document);

        Document actual =
                documentHelper.createDocument("name", "url", fromDate, toDate);

        assertThat(actual).isSameAs(document);
    }

    @Test
    public void testCreateDocumentNullParams() {
        try {
            documentHelper.createDocument(null, "y", new Date(), new Date());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            documentHelper.createDocument("x", null, new Date(), new Date());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("url must not be null");
        }

        try {
            documentHelper.createDocument("x", "y", null, new Date());
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("fromDate must not be null");
        }

        try {
            documentHelper.createDocument("x", "y", new Date(), null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("toDate must not be null");
        }
    }

    private List<Document> getTestDocuments() throws ParseException {

        String[] parsePatterns = {"dd-MM-yyyy HH:mm:ss.SSS"};
        Document doc1 =
                factory.createDocument("name", "url", new Date(), new Date());
        doc1.setId(1L);
        Date toDate =
                DateUtils.parseDate("01-07-2017 09:59:59.999", parsePatterns);
        doc1.setToDate(toDate);

        Document doc2 =
                factory.createDocument("name", "url", new Date(), new Date());
        doc2.setId(2L);
        toDate = DateUtils.parseDate("01-07-2017 10:00:00.000", parsePatterns);
        doc2.setToDate(toDate);

        List<Document> documents = new ArrayList<>();
        documents.add(doc1);
        documents.add(doc2);
        return documents;
    }
}
