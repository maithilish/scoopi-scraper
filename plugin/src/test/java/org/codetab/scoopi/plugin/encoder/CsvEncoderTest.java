package org.codetab.scoopi.plugin.encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CsvEncoderTest {
    @InjectMocks
    private CsvEncoder csvEncoder;

    @Mock
    private TaskInfo taskInfo;
    @Mock
    private IPluginDef pluginDef;
    @Mock
    private Plugin plugin;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEncode() {
        Data data = Mockito.mock(Data.class);

        String delimiter = ",";
        String includeTags = "false";

        Item item = Mockito.mock(Item.class);
        List<Item> orange = new ArrayList<>();
        orange.add(item);

        String kiwi = "Baz";
        String mango = "Qux";

        Axis axis = Mockito.mock(Axis.class);
        List<Axis> lychee = new ArrayList<>();
        lychee.add(axis);

        String tisrsjgt = "Quux";

        List<String> encodedData = new ArrayList<>();
        encodedData.add(String.join(",", kiwi, mango, tisrsjgt));

        when(pluginDef.getValue(plugin, "delimiter", ","))
                .thenReturn(delimiter);
        when(pluginDef.getValue(plugin, "includeTags", "false"))
                .thenReturn(includeTags);
        when(data.getItems()).thenReturn(orange);
        when(taskInfo.getName()).thenReturn(kiwi);
        when(taskInfo.getGroup()).thenReturn(mango);

        when(item.getAxes()).thenReturn(lychee);
        when(axis.getValue()).thenReturn(tisrsjgt);

        List<String> actual = csvEncoder.encode(data);

        assertEquals(encodedData, actual);
    }

    @Test
    public void testEncodeIncludeTags() {
        Data data = Mockito.mock(Data.class);

        String delimiter = ",";
        String includeTags = "true";

        Item item = Mockito.mock(Item.class);
        List<Item> orange = new ArrayList<>();
        orange.add(item);

        String kiwi = "Baz";
        String mango = "Qux";
        Data banana = Mockito.mock(Data.class);
        String cherry = "page-tag";
        Data apricot = Mockito.mock(Data.class);
        String peach = "index-tag";
        Data fig = Mockito.mock(Data.class);
        String plum = "item-tag";
        Axis axis = Mockito.mock(Axis.class);
        List<Axis> lychee = new ArrayList<>();
        lychee.add(axis);

        String tisrsjgt = "Quux";

        List<String> encodedData = new ArrayList<>();
        encodedData.add(
                String.join(",", kiwi, mango, cherry, peach, plum, tisrsjgt));

        when(pluginDef.getValue(plugin, "delimiter", ","))
                .thenReturn(delimiter);
        when(pluginDef.getValue(plugin, "includeTags", "false"))
                .thenReturn(includeTags);
        when(data.getItems()).thenReturn(orange);
        when(taskInfo.getName()).thenReturn(kiwi);
        when(taskInfo.getGroup()).thenReturn(mango);

        when(item.getParent()).thenReturn(banana).thenReturn(apricot)
                .thenReturn(fig);
        when(banana.getTagValue("page")).thenReturn(cherry);
        when(apricot.getTagValue("index")).thenReturn(peach);
        when(fig.getTagValue("item")).thenReturn(plum);

        when(item.getAxes()).thenReturn(lychee);
        when(axis.getValue()).thenReturn(tisrsjgt);

        List<String> actual = csvEncoder.encode(data);

        assertEquals(encodedData, actual);
    }

    @Test
    public void testSetPlugin() throws IllegalAccessException {
        Plugin p = Mockito.mock(Plugin.class);
        csvEncoder.setPlugin(p);
        assertSame(p, FieldUtils.readDeclaredField(csvEncoder, "plugin", true));
    }
}
