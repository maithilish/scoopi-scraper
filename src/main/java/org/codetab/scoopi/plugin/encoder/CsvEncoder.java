package org.codetab.scoopi.plugin.encoder;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.TaskInfo;

public class CsvEncoder implements IEncoder<List<String>> {

    @Inject
    private TaskInfo taskInfo;
    @Inject
    private IPluginDefs pluginDefs;
    @Inject
    private DataSorter dataSorter;

    private Plugin plugin;

    @Override
    public List<String> encode(final Data data) {

        notNull(data, "data must not be null");
        validState(nonNull(plugin), "plugin is not set");

        dataSorter.sort(data, plugin);

        List<String> encodedData = new ArrayList<>();

        String delimiter = pluginDefs.getValue(plugin, "delimiter", ",");
        boolean outputTags =
                Boolean.valueOf(pluginDefs.getValue(plugin, "tags", "true"));

        // encode and append data
        for (Item item : data.getItems()) {
            String col = item.getValue(AxisName.COL);
            String row = item.getValue(AxisName.ROW);
            String fact = item.getValue(AxisName.FACT);

            StringBuilder sb = new StringBuilder();
            sb.append(taskInfo.getName());
            sb.append(delimiter);
            sb.append(taskInfo.getGroup());
            sb.append(delimiter);
            if (outputTags) {
                sb.append(item.getParent().getTagValue("page"));
                sb.append(delimiter);
                sb.append(item.getParent().getTagValue("index"));
                sb.append(delimiter);
                sb.append(item.getParent().getTagValue("item"));
                sb.append(delimiter);
            }
            sb.append(col);
            sb.append(delimiter);
            sb.append(row);
            sb.append(delimiter);
            sb.append(fact);

            encodedData.add(sb.toString());
        }
        return encodedData;
    }

    @Override
    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }
}
