package org.codetab.scoopi.defs.yml;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskDefTem {

    @Inject
    private TaskDefData data;

    public Optional<String> getFirstTaskName(final String taskGroup) {
        List<String> taskNames = data.getTaskNamesMap().get(taskGroup);
        if (isNull(taskNames) || taskNames.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(taskNames.get(0));
        }
    }

}
