package org.codetab.scoopi.step.load;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.plugin.appender.Appender;
import org.codetab.scoopi.plugin.encoder.IEncoder;
import org.codetab.scoopi.step.base.BaseAppender;
import org.codetab.scoopi.system.ErrorLogger;
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
    private ErrorLogger errorLogger;

    @Override
    public boolean process() {
        for (String appenderName : appenders.keySet()) {
            try {
                Appender appender = appenders.get(appenderName);
                List<IEncoder<?>> encodersList = encoders.get(appenderName);
                Object encodedData = encode(encodersList);
                boolean stream = true;
                try {
                    stream = Boolean.valueOf(appender.getPluginField("stream"));
                } catch (DefNotFoundException e) {
                }

                if (encodedData instanceof Collection) {
                    Collection<?> list = (Collection<?>) encodedData;
                    if (stream) {
                        // stream
                        for (Object obj : list) {
                            doAppend(appender, obj);
                        }
                    } else {
                        // bulk load
                        doAppend(appender, list);
                    }
                } else {
                    doAppend(appender, encodedData);
                }
            } catch (Exception e) {
                String message =
                        String.join(" ", "unable to append to:", appenderName);
                errorLogger.log(getMarker(), CAT.ERROR, getLabeled(message), e);
            }
        }
        setOutput(data);
        setConsistent(true);
        // trace();
        return true;
    }
}
