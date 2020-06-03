package org.codetab.scoopi.step.load;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.exception.JobRunException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender;
import org.codetab.scoopi.plugin.encoder.IEncoder;
import org.codetab.scoopi.step.base.BaseAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encode and append Data objects to defined appenders
 * @author Maithilish
 *
 */
public class DataAppender extends BaseAppender {

    static final Logger LOGGER = LoggerFactory.getLogger(DataAppender.class);

    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private ErrorLogger errorLogger;

    @Override
    public boolean process() {
        List<PrintPayload> printPayloads = new ArrayList<>();
        for (String appenderName : appenders.keySet()) {
            try {
                Appender appender = appenders.get(appenderName);
                List<IEncoder<?>> encodersList = encoders.get(appenderName);
                Object encodedData = encode(encodersList);

                PrintPayload printPayload = objectFactory.createPrintPayload(
                        getPayload().getJobInfo(), encodedData);

                // FIXME whether streaming is required
                doAppend(appender, printPayload);
                printPayloads.add(printPayload);

            } catch (Exception e) {
                String message = spaceit("unable to append to:", appenderName);
                errorLogger.log(getMarker(), CAT.ERROR, getLabeled(message), e);
            }
        }
        boolean appendError = false;
        try {
            for (PrintPayload printPayload : printPayloads) {
                if (!printPayload.isFinished()) {
                    appendError = true;
                }
            }
        } catch (InterruptedException e) {
            appendError = true;
        }
        if (appendError) {
            throw new JobRunException("unable to append data to an appender");
        }
        setOutput(data);
        setConsistent(true);
        // trace();
        return true;
    }
}
