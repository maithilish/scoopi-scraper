package org.codetab.scoopi.step;

import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;

public interface IStep {

    void setup();

    void initialize();

    void load();

    void store();

    void process();

    void handover();

    void setPayload(Payload payload);

    Payload getPayload();

    void setOutput(Object data);

    Object getOutput();

    JobInfo getJobInfo();

    StepInfo getStepInfo();

    String getStepName();

    Marker getJobMarker();

    String getLabel();

    String getLabeled(String message);

    Marker getJobAbortedMarker();
}
