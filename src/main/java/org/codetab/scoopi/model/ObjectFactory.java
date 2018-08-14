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
            final String group, final String task, final String dataDef) {
        return new JobInfo(id, locator, group, task, dataDef);
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

    public Data createData(final String name, final String dataDef,
            final long documentId, final long dataDefId) {
        Data data = new Data();
        data.setName(name);
        data.setDataDef(dataDef);
        data.setDocumentId(documentId);
        data.setDataDefId(dataDefId);
        return data;
    }

    public Member createMember() {
        Member member = new Member();
        return member;
    }

    public Axis createAxis(final AxisName name) {
        Axis axis = new Axis(name);
        return axis;
    }
}
