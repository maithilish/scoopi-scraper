package org.codetab.scoopi.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

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

    public JobInfo createJobInfo(final long id, final String locator,
            final String group, final String task, final String steps,
            final String dataDef) {
        return new JobInfo(id, locator, group, task, steps, dataDef);
    }

    public Payload createPayload(final JobInfo jobInfo, final StepInfo stepInfo,
            final Object data) {
        return new Payload(jobInfo, stepInfo, data);
    }

    public LocatorGroup createLocatorGroup(final String group) {
        LocatorGroup lg = new LocatorGroup();
        lg.setGroup(group);
        return lg;
    }

    public Locator createLocator(final String name, final String group,
            final String url) {
        Locator locator = new Locator();
        locator.setName(name);
        locator.setGroup(group);
        locator.setUrl(url);
        return locator;
    }

    public Document createDocument(final String name, final String url,
            final Date fromDate, final Date toDate) {
        Document document = new Document();
        document.setName(name);
        document.setUrl(url);
        document.setFromDate(DateUtils.truncate(fromDate, Calendar.SECOND));
        document.setToDate(DateUtils.truncate(toDate, Calendar.SECOND));
        return document;
    }

    public Data createData(final String dataDef) {
        Data data = new Data();
        data.setDataDef(dataDef);
        return data;
    }

    public Item createItem() {
        Item item = new Item();
        return item;
    }

    public Axis createAxis(final String name, final String itemName) {
        Axis axis = new Axis(name, itemName);
        return axis;
    }

    public Axis createAxis(final String name, final String itemName,
            final String value, final String match, final int index,
            final int order) {
        Axis axis = new Axis(name, itemName);
        axis.setValue(value);
        axis.setMatch(match);
        axis.setIndex(index);
        axis.setOrder(order);
        return axis;
    }

    public Axis createAxis(final String name, final String itemName,
            final String value, final String match, final Integer index,
            final Integer order) {
        Axis axis = new Axis(name, itemName);
        axis.setValue(value);
        axis.setMatch(match);
        axis.setIndex(index);
        axis.setOrder(order);
        return axis;
    }

    public DataDef createDataDef(final String name, final Date fromDate,
            final Date toDate, final String defJson) {
        DataDef dataDef = new DataDef();
        dataDef.setName(name);
        dataDef.setFromDate(fromDate);
        dataDef.setToDate(toDate);
        dataDef.setDefJson(defJson);
        return dataDef;
    }

    public DataDef createDataDef(final String name) {
        DataDef dataDef = new DataDef();
        dataDef.setName(name);
        return dataDef;
    }

    public Filter createFilter(final String type, final String pattern) {
        Filter filter = new Filter();
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
}
