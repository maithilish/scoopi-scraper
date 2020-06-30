package org.codetab.scoopi.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.codetab.scoopi.model.helper.Fingerprints;

/**
 * Factory to create model objects.
 * <p>
 * JDO Persistable classes are regular POJO with getters and setters as fields
 * can't be final.
 * </p>
 * <p>
 * Other classes are either immutable or with some final fields.
 * </p>
 * @author maithilish
 *
 */
public class ObjectFactory {

    public StepInfo createStepInfo(final String stepName,
            final String priviousStepName, final String nextStepName,
            final String className) {
        return new StepInfo(stepName, priviousStepName, nextStepName,
                className);
    }

    public JobInfo createJobInfo(final String locator, final String group,
            final String task, final String steps, final String dataDef) {
        return new JobInfo(locator, group, task, steps, dataDef);
    }

    public Payload createPayload(final JobInfo jobInfo, final StepInfo stepInfo,
            final Object data) {
        return new Payload(jobInfo, stepInfo, data);
    }

    public PrintPayload createPrintPayload(final JobInfo jobInfo,
            final Object data) {
        return new PrintPayload(jobInfo, data);
    }

    public LocatorGroup createLocatorGroup(final String group) {
        final LocatorGroup lg = new LocatorGroup();
        lg.setGroup(group);
        return lg;
    }

    public Locator createLocator(final String name, final String group,
            final String url) {
        final Locator locator = new Locator();
        locator.setName(name);
        locator.setGroup(group);
        locator.setUrl(url);
        String fingerprint = Fingerprints.fingerprint(name.getBytes(),
                group.getBytes(), url.getBytes());
        locator.setFingerprint(new Fingerprint(fingerprint));
        return locator;
    }

    public Document createDocument(final String name, final String url,
            final Date fromDate, final Date toDate) {
        final Document document = new Document();
        document.setName(name);
        document.setUrl(url);
        document.setFromDate(DateUtils.truncate(fromDate, Calendar.SECOND));
        document.setToDate(DateUtils.truncate(toDate, Calendar.SECOND));
        return document;
    }

    public Metadata createMetadata(final Fingerprint locatorFp,
            final Fingerprint locatorWithDataFp, final Date documentDate) {
        Metadata metadata = new Metadata();
        metadata.setLocator(locatorWithDataFp);
        metadata.setDocumentDate(documentDate);
        metadata.setFingerprint(locatorFp);
        return metadata;
    }

    public Data createData(final String dataDef) {
        final Data data = new Data();
        data.setDataDef(dataDef);
        return data;
    }

    public Item createItem() {
        final Item item = new Item();
        return item;
    }

    public Axis createAxis(final String name, final String itemName) {
        final Axis axis = new Axis(name, itemName);
        return axis;
    }

    public Axis createAxis(final String name, final String itemName,
            final String value, final String match, final int index,
            final int order) {
        final Axis axis = new Axis(name, itemName);
        axis.setValue(value);
        axis.setMatch(match);
        axis.setIndex(index);
        axis.setOrder(order);
        return axis;
    }

    public Axis createAxis(final String name, final String itemName,
            final String value, final String match, final Integer index,
            final Integer order) {
        final Axis axis = new Axis(name, itemName);
        axis.setValue(value);
        axis.setMatch(match);
        axis.setIndex(index);
        axis.setOrder(order);
        return axis;
    }

    public DataDef createDataDef(final String name, final Date fromDate,
            final Date toDate, final String defJson) {
        final DataDef dataDef = new DataDef();
        dataDef.setName(name);
        dataDef.setFromDate(fromDate);
        dataDef.setToDate(toDate);
        dataDef.setDefJson(defJson);
        return dataDef;
    }

    public DataDef createDataDef(final String name) {
        final DataDef dataDef = new DataDef();
        dataDef.setName(name);
        return dataDef;
    }

    public Filter createFilter(final String type, final String pattern) {
        final Filter filter = new Filter();
        filter.setType(type);
        filter.setPattern(pattern);
        return filter;
    }

    public Plugin createPlugin(final String name, final String clzName,
            final String taskGroup, final String taskName,
            final String stepName, final String defJson, final Object def) {
        return new Plugin(name, clzName, taskGroup, taskName, stepName, defJson,
                def);
    }

    public Query createQuery() {
        return new Query();
    }

    public ClusterJob createClusterJob(final long jobId) {
        return new ClusterJob(jobId);
    }
}
