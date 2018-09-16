package org.codetab.scoopi.step;

import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.slf4j.Marker;

public interface IStep {

    boolean setup();

    boolean initialize();

    boolean load();

    boolean store();

    boolean process();

    boolean handover();

    boolean isConsistent();

    void setConsistent(boolean consistent);

    Payload getPayload();

    void setPayload(Payload payload);

    JobInfo getJobInfo();

    StepInfo getStepInfo();

    String getStepName();

    Marker getMarker();

    String getLabel();

    String getLabeled(String message);

    Object getOutput();

    void setOutput(Object data);
}
