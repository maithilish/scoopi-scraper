package org.codetab.scoopi.step.base;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.JobInfo;

public class Persists {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ITaskDef taskDef;
    @Inject
    private Configs configs;

    public boolean persistDocument() {
        if (!configs.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configs.isPersist("scoopi.persist.locator")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        // enabled at global and model level
        return true;
    }

    public boolean persistData(final JobInfo jobInfo) {
        // TODO write itest and verify Ex-12
        String taskGroup = jobInfo.getGroup();
        String taskName = jobInfo.getTask();
        Boolean persistData = true;
        try {
            String v = taskDef.getFieldValue(taskGroup, taskName, "persist",
                    "data");
            // modified for coverage - refactor this
            if (isNull(v)) {
                persistData = null;
            } else {
                persistData = Boolean.valueOf(v);
            }
        } catch (DefNotFoundException e) {
        } catch (IOException e) {
            LOG.error("get persist for {} {}, {}", taskGroup, taskName,
                    e.getMessage());
        }

        Optional<Boolean> taskLevelPersistence =
                Optional.ofNullable(persistData);

        if (!configs.useDataStore()) {
            // disabled at global level
            return false;
        }
        if (!configs.isPersist("scoopi.persist.data")) { //$NON-NLS-1$
            // enabled at global but disabled at model level
            return false;
        }
        if (taskLevelPersistence.isPresent()) {
            // enabled at global and model level
            // enabled or disabled at task level
            return taskLevelPersistence.get();
        } else {
            // undefined at task level
            return true;
        }
    }
}
