package org.codetab.scoopi.model;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class ModelFactory {

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
}
