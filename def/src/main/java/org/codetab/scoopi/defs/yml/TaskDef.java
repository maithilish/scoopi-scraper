package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.model.StepInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

@Singleton
public class TaskDef implements ITaskDef {

    @Inject
    private Jacksons jacksons;
    @Inject
    private Yamls yamls;
    @Inject
    private TaskDefData data;

    private JsonNode defs;

    @Override
    public List<String> getTaskNames(final String taskGroup) {
        return Collections
                .unmodifiableList(data.getTaskNamesMap().get(taskGroup));
    }

    @Override
    public Optional<String> getFirstTaskName(final String taskGroup) {
        List<String> taskNames = data.getTaskNamesMap().get(taskGroup);
        if (isNull(taskNames) || taskNames.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(taskNames.get(0));
        }
    }

    @Override
    public String getFieldValue(final String taskGroup, final String taskName,
            final String... fieldNames)
            throws DefNotFoundException, IOException {
        ArrayList<String> parts = Lists.newArrayList(taskGroup, taskName);
        Collections.addAll(parts, fieldNames);
        String[] pathParts = new String[parts.size()];
        parts.toArray(pathParts);

        // reconstruct defs on deserialization
        if (isNull(defs)) {
            defs = yamls.toJsonNode(data.getDefsJson());
        }

        return jacksons.getFieldValue(defs, pathParts);
    }

    @Override
    public String getLive(final String taskGroup)
            throws DefNotFoundException, IOException {
        // reconstruct defs on deserialization
        if (isNull(defs)) {
            defs = yamls.toJsonNode(data.getDefsJson());
        }
        return jacksons.getFieldValue(defs, taskGroup, "live");
    }

    @Override
    public String getStepsName(final String taskGroup, final String taskName)
            throws DefNotFoundException {
        String key = dashit(taskGroup, taskName);
        return data.getStepsNameMap().get(key);
    }

    @Override
    public StepInfo getNextStep(final String taskGroup, final String taskName,
            final String stepName) throws DefNotFoundException {
        String key = dashit(taskGroup, taskName);
        return data.getNextStepsMap().get(key).get(stepName);
    }

}
