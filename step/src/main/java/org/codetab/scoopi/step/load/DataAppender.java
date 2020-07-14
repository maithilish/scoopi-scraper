package org.codetab.scoopi.step.load;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.exception.JobRunException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.PrintPayload;
import org.codetab.scoopi.plugin.appender.Appender;
import org.codetab.scoopi.plugin.encoder.IEncoder;
import org.codetab.scoopi.step.base.BaseAppender;

/**
 * Encode and append Data objects to defined appenders
 * @author Maithilish
 *
 */
public class DataAppender extends BaseAppender {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ObjectFactory objectFactory;
    @Inject
    private Errors errors;

    @Override
    public void process() {
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
                errors.inc();
                LOG.error(getJobAbortedMarker(), "{} [{}]", getLabeled(message),
                        ERROR.DATAERROR, e);
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
    }
}
