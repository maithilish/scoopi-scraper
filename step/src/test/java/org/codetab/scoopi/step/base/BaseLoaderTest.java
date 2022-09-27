package org.codetab.scoopi.step.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.JobStateException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.model.helper.Documents;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BaseLoaderTest {

    @InjectMocks
    private TestBaseLoader baseLoader;

    @Mock
    private IDocumentDao documentDao;
    @Mock
    private Documents documents;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private PayloadFactory payloadFactory;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private Persists persists;
    @Mock
    private FetchThrottle fetchThrottle;
    @Mock
    private Locator locator;
    @Mock
    private Document document;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private ObjectFactory factory;
    @Mock
    private Document output;
    @Mock
    private Payload payload;
    @Mock
    private Marker jobMarker;
    @Mock
    private Marker jobAbortedMarker;
    @Mock
    private IOHelper ioHelper;

    /**
     * BaseLoader is abstract, extend it and test TestBaseLoader.
     * @author m
     *
     */
    static class TestBaseLoader extends BaseLoader {
        @Inject
        private IOHelper ioHelper;

        @Override
        public byte[] fetchDocumentObject(final String url) throws IOException {
            final URL fileURL = ioHelper.getResourceURL(url);
            return ioHelper.toByteArray(fileURL);
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitializeIf() throws Exception {
        Object grape = Mockito.mock(Object.class);
        Locator pData = Mockito.mock(Locator.class);
        boolean persist = true;

        when(payload.getData()).thenReturn(grape).thenReturn(pData);
        when(persists.persistDocument()).thenReturn(persist);
        baseLoader.initialize();

        assertTrue((boolean) FieldUtils.readField(baseLoader, "persist", true));
        assertSame(pData, FieldUtils.readField(baseLoader, "locator", true));
    }

    @Test
    public void testInitializeElse() {
        Object grape = Mockito.mock(Object.class);
        boolean persist = true;

        // pData is not instance of Locator
        when(payload.getData()).thenReturn(grape);
        when(persists.persistDocument()).thenReturn(persist);
        assertThrows(StepRunException.class, () -> baseLoader.initialize());
    }

    @Test
    public void testLoadIfDocumentsIsDocumentLiveTryIfIsNull()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String live = "PT0S";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";
        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;
        Document document1 = null;
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";
        String lychee = "Corge";

        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenReturn(document1);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);

        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadIfDocumentsIsDocumentLiveTryElseIsNull()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String live = "PT0S";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";
        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;
        Document document1 = Mockito.mock(Document.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";
        String lychee = "Corge";
        String bionic = "GraultGraultGrault";

        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenReturn(document1);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);
        when(locatorFp.getValue()).thenReturn(bionic);

        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao, never()).delete(locatorFp);
    }

    @Test
    public void testLoadIfDocumentsIsDocumentLiveTryCatchChecksumException()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String live = "PT0S";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";
        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);

        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;

        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";

        String lychee = "Corge";

        FieldUtils.writeField(baseLoader, "document", null, true);
        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenThrow(ChecksumException.class);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);

        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadTryTryElseDocumentsIsDocumentLive() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String live = "PT0S";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = false;
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String lychee = "Corge";

        FieldUtils.writeField(baseLoader, "document", null, true);
        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo2.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo3))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo4.getLabel()).thenReturn(lychee);
        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao, never()).get(locatorFp);
        verify(documentDao).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadTryTryCatchDaoException() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";

        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);

        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";

        String lychee = "Corge";

        String live = "PT0S";
        FieldUtils.writeField(baseLoader, "document", null, true);
        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);
        when(documentDao.get(locatorFp)).thenThrow(DaoException.class);

        assertThrows(StepRunException.class, () -> baseLoader.load());

        verify(configs, never()).getRunDateTime();
        verify(documentDao, never()).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadTryCatchDaoExceptionTry() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";

        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);

        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;

        Document document1 = null; // null path

        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";

        String lychee = "Corge";

        String live = "PT0S";
        FieldUtils.writeField(baseLoader, "document", null, true);
        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);

        when(documentDao.getDocumentDate(locatorFp))
                .thenThrow(DaoException.class);

        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(configs.getRunDateTime()).thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenReturn(document1);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);

        baseLoader.load();

        verify(documentDao).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadCatchDefNotFoundExceptionTryTry() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";

        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);

        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;
        Document document1 = null; // isNull path
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";

        String lychee = "Corge";

        String live = "PT0S";
        FieldUtils.writeField(baseLoader, "document", null, true);
        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenThrow(DefNotFoundException.class);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenReturn(document1);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);

        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadCatchIOExceptionTryTry() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";

        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";

        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);

        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;
        Document document1 = null; // isNull path
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";

        String lychee = "Corge";

        String live = "PT0S";
        FieldUtils.writeField(baseLoader, "document", null, true);
        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenThrow(IOException.class);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenReturn(document1);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo.getLabel()).thenReturn(lychee);

        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testLoadTryTryTry() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String taskGroup = "Foo";
        String live = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Baz";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String banana = "Qux";
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        ZonedDateTime documentToDate = Mockito.mock(ZonedDateTime.class);
        boolean apricot = true;
        Document document1 = Mockito.mock(Document.class);
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String plum = "Quux";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String lychee = "Corge";

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4);
        when(jobInfo.getGroup()).thenReturn(taskGroup);
        when(taskDef.getLive(taskGroup)).thenReturn(live);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(jobInfo2.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.getDocumentDate(locatorFp)).thenReturn(documentDate);
        when(documents.getToDate(documentDate, live, jobInfo3))
                .thenReturn(documentToDate);
        when(documents.isDocumentLive(documentToDate)).thenReturn(apricot);
        when(documentDao.get(locatorFp)).thenReturn(document1);
        when(stepInfo2.getStepName()).thenReturn(plum);
        when(jobInfo4.getLabel()).thenReturn(lychee);
        baseLoader.load();

        verify(configs, never()).getRunDateTime();
        verify(documentDao, never()).delete(locatorFp);
        verify(locatorFp, never()).getValue();
    }

    @Test
    public void testProcessIfFetchDocumentTry() throws Exception {
        String grape = "Foo";
        byte[] documentObject = {'a'};
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        String orange = "Bar";
        String kiwi = "Baz";
        String mango = "Qux";
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        Document newDocument = Mockito.mock(Document.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String apricot = "Corge";

        URL url = Mockito.mock(URL.class);
        when(baseLoader.ioHelper.getResourceURL(grape)).thenReturn(url);
        when(baseLoader.ioHelper.toByteArray(url)).thenReturn(documentObject);

        when(locator.getUrl()).thenReturn(grape).thenReturn(kiwi);
        when(configs.getRunDateTime()).thenReturn(documentDate);
        when(locator.getName()).thenReturn(orange);
        when(locator.getGroup()).thenReturn(mango);
        when(locator.getFingerprint()).thenReturn(fingerprint);
        when(objectFactory.createDocument(orange, documentDate, kiwi, mango,
                fingerprint)).thenReturn(newDocument);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(cherry);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(apricot);

        baseLoader.process();

        verify(fetchThrottle).acquirePermit();
        verify(fetchThrottle).releasePermit();
        verify(newDocument).setDocumentObject(documentObject);
    }

    @Test
    public void testProcessIfFetchDocumentTryCatchIOException()
            throws Exception {
        String grape = "Foo";
        byte[] documentObject = {'a'};
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        String orange = "Bar";
        String kiwi = "Baz";
        String mango = "Qux";
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        Document newDocument = Mockito.mock(Document.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String apricot = "Corge";

        when(locator.getUrl()).thenReturn(grape).thenReturn(kiwi);
        when(configs.getRunDateTime()).thenReturn(documentDate);
        when(locator.getName()).thenReturn(orange);
        when(locator.getGroup()).thenReturn(mango);
        when(locator.getFingerprint()).thenReturn(fingerprint);
        when(objectFactory.createDocument(orange, documentDate, kiwi, mango,
                fingerprint)).thenReturn(newDocument);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(cherry);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(apricot);

        URL url = Mockito.mock(URL.class);
        when(baseLoader.ioHelper.getResourceURL(grape)).thenReturn(url);
        when(baseLoader.ioHelper.toByteArray(url)).thenThrow(IOException.class);

        assertThrows(StepRunException.class, () -> baseLoader.process());

        verify(fetchThrottle).acquirePermit();
        verify(fetchThrottle).releasePermit();
        verify(newDocument, never()).setDocumentObject(documentObject);
    }

    @Test
    public void testProcessElseFetchDocument() throws Exception {
        byte[] documentObject = {};
        ZonedDateTime documentDate = Mockito.mock(ZonedDateTime.class);
        String orange = "Foo";
        String kiwi = "Bar";
        String mango = "Baz";
        Fingerprint fingerprint = Mockito.mock(Fingerprint.class);
        Document newDocument = Mockito.mock(Document.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String cherry = "Qux";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String apricot = "Quux";

        FieldUtils.writeField(baseLoader, "fetchDocument", false, true);

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(cherry);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(apricot);

        baseLoader.process();

        verify(fetchThrottle, never()).acquirePermit();
        verify(locator, never()).getUrl();
        verify(fetchThrottle, never()).releasePermit();
        verify(configs, never()).getRunDateTime();
        verify(locator, never()).getName();
        verify(locator, never()).getUrl();
        verify(locator, never()).getGroup();
        verify(locator, never()).getFingerprint();
        verify(objectFactory, never()).createDocument(orange, documentDate,
                kiwi, mango, fingerprint);
        verify(newDocument, never()).setDocumentObject(documentObject);
    }

    @Test
    public void testStoreTryIfFetchDocument() throws Exception {
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String banana = "Bar";

        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(banana);

        baseLoader.store();

        verify(documentDao).save(locatorFp, document);
    }

    @Test
    public void testStoreTryElseFetchDocument() throws Exception {
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String banana = "Bar";

        FieldUtils.writeField(baseLoader, "persist", false, true);
        FieldUtils.writeField(baseLoader, "fetchDocument", true, true);

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(banana);
        baseLoader.store();

        verify(locator, never()).getFingerprint();
        verify(documentDao, never()).save(locatorFp, document);
    }

    @Test
    public void testStoreTryElseFetchDocument2() throws Exception {
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String banana = "Bar";

        FieldUtils.writeField(baseLoader, "persist", false, true);
        FieldUtils.writeField(baseLoader, "fetchDocument", false, true);

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(banana);
        baseLoader.store();

        verify(locator, never()).getFingerprint();
        verify(documentDao, never()).save(locatorFp, document);
    }

    @Test
    public void testStoreTryCatchDaoException() throws Exception {
        Fingerprint locatorFp = Mockito.mock(Fingerprint.class);
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String mango = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String banana = "Bar";

        FieldUtils.writeField(baseLoader, "persist", true, true);

        when(payload.getStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getStepName()).thenReturn(mango);
        when(payload.getJobInfo()).thenReturn(jobInfo);
        when(jobInfo.getLabel()).thenReturn(banana);
        when(locator.getFingerprint()).thenReturn(locatorFp);
        when(documentDao.save(locatorFp, document))
                .thenThrow(DaoException.class);

        assertThrows(StepRunException.class, () -> baseLoader.store());

    }

    @Test
    public void testHandoverIfTaskNamesSizeOne() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        List<String> taskNames = new ArrayList<>();
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String groupMango = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String banana = "end";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String apricot = "Corge";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String fig = "Grault";
        JobInfo jobInfo7 = Mockito.mock(JobInfo.class);
        String plum = "Garply";
        String scrappy = "Waldo";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo8 = Mockito.mock(JobInfo.class);
        String bionic = "Fred";
        List<Payload> payloads = new ArrayList<>();
        Payload payload1 = Mockito.mock(Payload.class);
        JobInfo jobInfo9 = Mockito.mock(JobInfo.class);
        long bolt = 1L;
        JobInfo jobInfo10 = Mockito.mock(JobInfo.class);

        taskNames.add(taskName);

        payloads.add(payload1);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5)
                .thenReturn(jobInfo6).thenReturn(jobInfo7).thenReturn(jobInfo8);
        when(jobInfo.getGroup()).thenReturn(group);
        when(taskDef.getTaskNames(group)).thenReturn(taskNames);
        when(jobInfo2.getGroup()).thenReturn(groupMango);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(banana);
        when(jobInfo4.getId()).thenReturn(jobId);
        when(jobInfo5.getLabel()).thenReturn(apricot);
        when(taskDef.getNextStep(groupMango, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo6, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(fig);
        when(jobInfo7.getLabel()).thenReturn(plum);
        when(nextStep.getStepName()).thenReturn(scrappy);

        baseLoader.handover();

        verify(jobMediator).markJobFinished(jobId);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(configs, never()).isCluster();
        verify(jobInfo8, never()).getName();
        verify(payloadFactory, never()).createPayloads(group, taskNames,
                stepInfo4, bionic, output);
        verify(payload1, never()).getJobInfo();
        verify(jobMediator, never()).getJobIdSequence();
        verify(jobInfo9, never()).setId(bolt);
        verify(payload1, never()).getJobInfo();
        verify(jobInfo10, never()).getId();
        verify(jobMediator, never()).pushJobs(payloads, jobId);
    }

    @Test
    public void testHandoverIfTaskNamesSizeOneNextStepNotEnd()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        List<String> taskNames = new ArrayList<>();
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String groupMango = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String banana = "notEnd";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        long jobId = 1L;
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String apricot = "Corge";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String fig = "Grault";
        JobInfo jobInfo7 = Mockito.mock(JobInfo.class);
        String plum = "Garply";
        String scrappy = "Waldo";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo8 = Mockito.mock(JobInfo.class);
        String bionic = "Fred";
        List<Payload> payloads = new ArrayList<>();
        Payload payload1 = Mockito.mock(Payload.class);
        JobInfo jobInfo9 = Mockito.mock(JobInfo.class);
        long bolt = 1L;
        JobInfo jobInfo10 = Mockito.mock(JobInfo.class);

        taskNames.add(taskName);

        payloads.add(payload1);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo6).thenReturn(jobInfo7)
                .thenReturn(jobInfo8);
        when(jobInfo.getGroup()).thenReturn(group);
        when(taskDef.getTaskNames(group)).thenReturn(taskNames);
        when(jobInfo2.getGroup()).thenReturn(groupMango);
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(banana);
        when(jobInfo4.getId()).thenReturn(jobId);
        when(jobInfo5.getLabel()).thenReturn(apricot);
        when(taskDef.getNextStep(groupMango, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo6, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(fig);
        when(jobInfo7.getLabel()).thenReturn(plum);
        when(nextStep.getStepName()).thenReturn(scrappy);

        baseLoader.handover();

        verify(jobMediator, never()).markJobFinished(jobId);
        verify(taskMediator).pushPayload(nextStepPayload);
        verify(configs, never()).isCluster();
        verify(jobInfo8, never()).getName();
        verify(payloadFactory, never()).createPayloads(group, taskNames,
                stepInfo4, bionic, output);
        verify(payload1, never()).getJobInfo();
        verify(jobMediator, never()).getJobIdSequence();
        verify(jobInfo9, never()).setId(bolt);
        verify(payload1, never()).getJobInfo();
        verify(jobInfo10, never()).getId();
        verify(jobMediator, never()).pushJobs(payloads, jobId);
    }

    // not super.handover
    @Test
    public void testHandoverElseTaskNamesSizeIfConfigsIsClusterTry()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        List<String> taskNames = new ArrayList<>();
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String groupMango = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        long jobId = 0L;
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String apricot = "Corge";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String fig = "Grault";
        JobInfo jobInfo7 = Mockito.mock(JobInfo.class);
        String plum = "Garply";
        String scrappy = "Waldo";
        boolean barracuda = true;
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo8 = Mockito.mock(JobInfo.class);
        String bionic = "Fred";
        List<Payload> payloads = new ArrayList<>();
        Payload payload1 = Mockito.mock(Payload.class);
        JobInfo jobInfo9 = Mockito.mock(JobInfo.class);
        long bolt = 1L;
        JobInfo jobInfo10 = Mockito.mock(JobInfo.class);

        output = document;
        baseLoader.setOutput(document);
        payloads.add(payload1);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo8)
                .thenReturn(jobInfo10);
        when(payload1.getJobInfo()).thenReturn(jobInfo9);
        when(jobInfo.getGroup()).thenReturn(group);
        when(taskDef.getTaskNames(group)).thenReturn(taskNames);
        when(jobInfo2.getGroup()).thenReturn(groupMango);
        when(payload.getStepInfo()).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(banana);
        when(jobInfo4.getId()).thenReturn(jobId);
        when(jobInfo5.getLabel()).thenReturn(apricot);
        when(taskDef.getNextStep(groupMango, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo6, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(fig);
        when(jobInfo7.getLabel()).thenReturn(plum);
        when(nextStep.getStepName()).thenReturn(scrappy);
        when(configs.isCluster()).thenReturn(barracuda);
        when(jobInfo8.getName()).thenReturn(bionic);
        when(payloadFactory.createPayloads(group, taskNames, stepInfo4, bionic,
                output)).thenReturn(payloads);
        when(jobMediator.getJobIdSequence()).thenReturn(bolt);
        when(jobInfo10.getId()).thenReturn(jobId);

        baseLoader.handover();

        verify(jobMediator, never()).markJobFinished(jobId);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(jobInfo9).setId(bolt);
        verify(jobMediator).pushJobs(payloads, jobId);
        verify(document).compress();
    }

    @Test
    public void testHandoverTryCatchInterruptedExceptionIf() throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        List<String> taskNames = new ArrayList<>();
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String groupMango = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        long jobId = 0L;
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String apricot = "Corge";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String fig = "Grault";
        JobInfo jobInfo7 = Mockito.mock(JobInfo.class);
        String plum = "Garply";
        String scrappy = "Waldo";
        boolean barracuda = true;
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo8 = Mockito.mock(JobInfo.class);
        String bionic = "Fred";
        List<Payload> payloads = new ArrayList<>();
        Payload payload1 = Mockito.mock(Payload.class);
        JobInfo jobInfo9 = Mockito.mock(JobInfo.class);
        long bolt = 1L;
        JobInfo jobInfo10 = Mockito.mock(JobInfo.class);

        output = document;
        baseLoader.setOutput(document);
        payloads.add(payload1);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo8)
                .thenReturn(jobInfo10);
        when(payload1.getJobInfo()).thenReturn(jobInfo9);
        when(jobInfo.getGroup()).thenReturn(group);
        when(taskDef.getTaskNames(group)).thenReturn(taskNames);
        when(jobInfo2.getGroup()).thenReturn(groupMango);
        when(payload.getStepInfo()).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(banana);
        when(jobInfo4.getId()).thenReturn(jobId);
        when(jobInfo5.getLabel()).thenReturn(apricot);
        when(taskDef.getNextStep(groupMango, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo6, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(fig);
        when(jobInfo7.getLabel()).thenReturn(plum);
        when(nextStep.getStepName()).thenReturn(scrappy);
        when(configs.isCluster()).thenReturn(barracuda);
        when(jobInfo8.getName()).thenReturn(bionic);
        when(payloadFactory.createPayloads(group, taskNames, stepInfo4, bionic,
                output)).thenReturn(payloads);
        when(jobMediator.getJobIdSequence()).thenReturn(bolt);
        when(jobInfo10.getId()).thenReturn(jobId);
        doThrow(InterruptedException.class).when(jobMediator).pushJobs(payloads,
                jobId);

        assertThrows(StepRunException.class, () -> baseLoader.handover());

        assertTrue(Thread.currentThread().isInterrupted());

        verify(jobMediator, never()).markJobFinished(jobId);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(jobInfo9).setId(bolt);
    }

    @Test
    public void testHandoverTryCatchInterruptedExceptionElse()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        List<String> taskNames = new ArrayList<>();
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String groupMango = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        long jobId = 0L;
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String apricot = "Corge";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String fig = "Grault";
        JobInfo jobInfo7 = Mockito.mock(JobInfo.class);
        String plum = "Garply";
        String scrappy = "Waldo";
        boolean barracuda = true;
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo8 = Mockito.mock(JobInfo.class);
        String bionic = "Fred";
        List<Payload> payloads = new ArrayList<>();
        Payload payload1 = Mockito.mock(Payload.class);
        JobInfo jobInfo9 = Mockito.mock(JobInfo.class);
        long bolt = 1L;
        JobInfo jobInfo10 = Mockito.mock(JobInfo.class);

        output = document;
        baseLoader.setOutput(document);
        payloads.add(payload1);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo8)
                .thenReturn(jobInfo10);
        when(payload1.getJobInfo()).thenReturn(jobInfo9);
        when(jobInfo.getGroup()).thenReturn(group);
        when(taskDef.getTaskNames(group)).thenReturn(taskNames);
        when(jobInfo2.getGroup()).thenReturn(groupMango);
        when(payload.getStepInfo()).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(banana);
        when(jobInfo4.getId()).thenReturn(jobId);
        when(jobInfo5.getLabel()).thenReturn(apricot);
        when(taskDef.getNextStep(groupMango, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo6, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(fig);
        when(jobInfo7.getLabel()).thenReturn(plum);
        when(nextStep.getStepName()).thenReturn(scrappy);
        when(configs.isCluster()).thenReturn(barracuda);
        when(jobInfo8.getName()).thenReturn(bionic);
        when(payloadFactory.createPayloads(group, taskNames, stepInfo4, bionic,
                output)).thenReturn(payloads);
        when(jobMediator.getJobIdSequence()).thenReturn(bolt);
        when(jobInfo10.getId()).thenReturn(jobId);
        doThrow(JobStateException.class).when(jobMediator).pushJobs(payloads,
                jobId);

        assertThrows(StepRunException.class, () -> baseLoader.handover());

        assertFalse(Thread.currentThread().isInterrupted());

        verify(jobMediator, never()).markJobFinished(jobId);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(jobInfo9).setId(bolt);
    }

    @Test
    public void testHandoverElseTaskNamesSizeElseConfigsIsClusterTry()
            throws Exception {
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String group = "Foo";
        List<String> taskNames = new ArrayList<>();
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String groupMango = "Bar";
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String stepName = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String banana = "Quux";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        long jobId = 0L;
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String apricot = "Corge";
        StepInfo nextStep = Mockito.mock(StepInfo.class);
        JobInfo jobInfo6 = Mockito.mock(JobInfo.class);
        Payload nextStepPayload = Mockito.mock(Payload.class);
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String fig = "Grault";
        JobInfo jobInfo7 = Mockito.mock(JobInfo.class);
        String plum = "Garply";
        String scrappy = "Waldo";
        boolean barracuda = false;
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        JobInfo jobInfo8 = Mockito.mock(JobInfo.class);
        String bionic = "Fred";
        List<Payload> payloads = new ArrayList<>();
        Payload payload1 = Mockito.mock(Payload.class);
        JobInfo jobInfo9 = Mockito.mock(JobInfo.class);
        long bolt = 1L;
        JobInfo jobInfo10 = Mockito.mock(JobInfo.class);

        output = document;
        baseLoader.setOutput(document);
        payloads.add(payload1);

        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo8)
                .thenReturn(jobInfo10);
        when(payload1.getJobInfo()).thenReturn(jobInfo9);
        when(jobInfo.getGroup()).thenReturn(group);
        when(taskDef.getTaskNames(group)).thenReturn(taskNames);
        when(jobInfo2.getGroup()).thenReturn(groupMango);
        when(payload.getStepInfo()).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(stepName);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getNextStepName()).thenReturn(banana);
        when(jobInfo4.getId()).thenReturn(jobId);
        when(jobInfo5.getLabel()).thenReturn(apricot);
        when(taskDef.getNextStep(groupMango, taskName, stepName))
                .thenReturn(nextStep);
        when(factory.createPayload(jobInfo6, nextStep, output))
                .thenReturn(nextStepPayload);
        when(stepInfo3.getStepName()).thenReturn(fig);
        when(jobInfo7.getLabel()).thenReturn(plum);
        when(nextStep.getStepName()).thenReturn(scrappy);
        when(configs.isCluster()).thenReturn(barracuda);
        when(jobInfo8.getName()).thenReturn(bionic);
        when(payloadFactory.createPayloads(group, taskNames, stepInfo4, bionic,
                output)).thenReturn(payloads);
        when(jobMediator.getJobIdSequence()).thenReturn(bolt);
        when(jobInfo10.getId()).thenReturn(jobId);

        baseLoader.handover();

        verify(jobMediator, never()).markJobFinished(jobId);
        verify(taskMediator, never()).pushPayload(nextStepPayload);
        verify(jobInfo9).setId(bolt);
        verify(jobMediator).pushJobs(payloads, jobId);
        verify(document, never()).compress();
    }

    @Test
    public void testIsDocumentLoaded() {

        boolean actual = baseLoader.isDocumentLoaded();

        assertTrue(actual);
    }

    // @Test
    // public void testFetchDocumentObject() throws Exception {
    // String url = "Foo";
    //
    // byte[] actual = baseLoader.fetchDocumentObject(url);
    // fail("unable to assert, STEPIN");
    // }
}
