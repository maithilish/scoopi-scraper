package org.codetab.scoopi.step.process;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.plugin.converter.ConverterMap;
import org.codetab.scoopi.plugin.converter.IConverter;
import org.codetab.scoopi.step.TestUtils;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DataConverterTest {
    @InjectMocks
    private DataConverter dataConverter;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private ConverterMap converterMap;
    @Mock
    private Data data;
    @Mock
    private Configs configs;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private ITaskDef taskDef;
    @Mock
    private TaskMediator taskMediator;
    @Mock
    private JobMediator jobMediator;
    @Mock
    private ObjectFactory factory;
    @Mock
    private Object output;
    @Mock
    private Payload payload;
    @Mock
    private Marker jobMarker;
    @Mock
    private Marker jobAbortedMarker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessTryIfPluginsIsPresent() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;

        Plugin plugin = Mockito.mock(Plugin.class);
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(pluginList);

        Item item = Mockito.mock(Item.class);
        List<Item> list2 = new ArrayList<>();
        list2.add(item);

        String itemName = "Corge";
        Set<String> set = new HashSet<>();
        set.add(itemName);

        Axis axis = Mockito.mock(Axis.class);
        String value = "Grault";

        List<IConverter> converters = new ArrayList<>();
        IConverter converter = Mockito.mock(IConverter.class);
        converters.add(converter);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(DataConverter.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(true);

        String cValue = "Garply";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bolt = "Waldo";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String bookworm = "Fred";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        String twilight = "Plugh";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String tuna = "Xyzzy";

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(data.getItems()).thenReturn(list2);
        when(converterMap.keySet()).thenReturn(set);
        when(item.getAxisByItemName(itemName)).thenReturn(axis);
        when(axis.getValue()).thenReturn(value);
        when(converterMap.get(itemName)).thenReturn(converters);
        when(converter.convert(value)).thenReturn(cValue);
        when(stepInfo3.getStepName()).thenReturn(bolt);
        when(jobInfo4.getLabel()).thenReturn(bookworm);
        when(stepInfo4.getStepName()).thenReturn(twilight);
        when(jobInfo5.getLabel()).thenReturn(tuna);

        dataConverter.process();

        assertSame(dataConverter.getOutput(), data);

        verify(converterMap).init(plugins.get());
        verify(axis).setValue(cValue);
    }

    @Test
    public void testProcessTryIfPluginsIsPresentNoConvert() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;

        Plugin plugin = Mockito.mock(Plugin.class);
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(pluginList);

        Item item = Mockito.mock(Item.class);
        List<Item> list2 = new ArrayList<>();
        list2.add(item);

        String itemName = "Corge";
        Set<String> set = new HashSet<>();
        set.add(itemName);

        Axis axis = Mockito.mock(Axis.class);

        List<IConverter> converters = new ArrayList<>();
        IConverter converter = Mockito.mock(IConverter.class);
        converters.add(converter);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(DataConverter.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(true);

        String value = "Grault";
        String cValue = value;

        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bolt = "Waldo";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String bookworm = "Fred";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        String twilight = "Plugh";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String tuna = "Xyzzy";

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(data.getItems()).thenReturn(list2);
        when(converterMap.keySet()).thenReturn(set);
        when(item.getAxisByItemName(itemName)).thenReturn(axis);
        when(axis.getValue()).thenReturn(value);
        when(converterMap.get(itemName)).thenReturn(converters);
        when(converter.convert(value)).thenReturn(cValue);
        when(stepInfo3.getStepName()).thenReturn(bolt);
        when(jobInfo4.getLabel()).thenReturn(bookworm);
        when(stepInfo4.getStepName()).thenReturn(twilight);
        when(jobInfo5.getLabel()).thenReturn(tuna);

        dataConverter.process();

        assertSame(dataConverter.getOutput(), data);

        verify(converterMap).init(plugins.get());
        verify(axis).setValue(cValue);
    }

    @Test
    public void testProcessTryIfPluginsIsPresentTraceNotEnabled()
            throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;

        Plugin plugin = Mockito.mock(Plugin.class);
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(pluginList);

        Item item = Mockito.mock(Item.class);
        List<Item> list2 = new ArrayList<>();
        list2.add(item);

        String itemName = "Corge";
        Set<String> set = new HashSet<>();
        set.add(itemName);

        Axis axis = Mockito.mock(Axis.class);

        List<IConverter> converters = new ArrayList<>();
        IConverter converter = Mockito.mock(IConverter.class);
        converters.add(converter);

        Logger log = Mockito.mock(Logger.class);
        TestUtils.setFinalStaticField(DataConverter.class, "LOG", log);
        when(log.isTraceEnabled()).thenReturn(false);

        String value = "Grault";
        String cValue = "Garply";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bolt = "Waldo";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String bookworm = "Fred";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        String twilight = "Plugh";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String tuna = "Xyzzy";

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(data.getItems()).thenReturn(list2);
        when(converterMap.keySet()).thenReturn(set);
        when(item.getAxisByItemName(itemName)).thenReturn(axis);
        when(axis.getValue()).thenReturn(value);
        when(converterMap.get(itemName)).thenReturn(converters);
        when(converter.convert(value)).thenReturn(cValue);
        when(stepInfo3.getStepName()).thenReturn(bolt);
        when(jobInfo4.getLabel()).thenReturn(bookworm);
        when(stepInfo4.getStepName()).thenReturn(twilight);
        when(jobInfo5.getLabel()).thenReturn(tuna);

        dataConverter.process();

        assertSame(dataConverter.getOutput(), data);

        verify(converterMap).init(plugins.get());
        verify(axis).setValue(cValue);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessTryElsePluginsIsPresent() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;
        Plugin plugin = Mockito.mock(Plugin.class);
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.empty();

        Item item = Mockito.mock(Item.class);
        String itemName = "Corge";
        Axis axis = Mockito.mock(Axis.class);
        IConverter converter = Mockito.mock(IConverter.class);
        String cValue = "Grault";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bolt = "Garply";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String bookworm = "Waldo";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        String twilight = "Fred";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String tuna = "Plugh";

        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(converter.convert(cValue)).thenReturn(cValue);
        when(stepInfo3.getStepName()).thenReturn(bolt);
        when(jobInfo4.getLabel()).thenReturn(bookworm);
        when(stepInfo4.getStepName()).thenReturn(twilight);
        when(jobInfo5.getLabel()).thenReturn(tuna);

        dataConverter.process();
        assertSame(dataConverter.getOutput(), data);

        verify(converterMap, never()).init(any(List.class));
        verify(data, never()).getItems();
        verify(converterMap, never()).keySet();
        verify(item, never()).getAxisByItemName(itemName);
        verify(axis, never()).getValue();
        verify(converterMap, never()).get(itemName);
        verify(axis, never()).setValue(cValue);
    }

    @Test
    public void testProcessTryCatchException() throws Exception {
        StepInfo stepInfo = Mockito.mock(StepInfo.class);
        String orange = "Foo";
        JobInfo jobInfo = Mockito.mock(JobInfo.class);
        String kiwi = "Bar";
        JobInfo jobInfo2 = Mockito.mock(JobInfo.class);
        String taskGroup = "Baz";
        JobInfo jobInfo3 = Mockito.mock(JobInfo.class);
        String taskName = "Qux";
        StepInfo stepInfo2 = Mockito.mock(StepInfo.class);
        String cherry = "Quux";
        String stepName = cherry;

        Plugin plugin = Mockito.mock(Plugin.class);
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(plugin);
        Optional<List<Plugin>> plugins = Optional.of(pluginList);

        Item item = Mockito.mock(Item.class);
        String itemName = "Corge";
        Axis axis = Mockito.mock(Axis.class);
        IConverter converter = Mockito.mock(IConverter.class);
        String cValue = "Grault";
        StepInfo stepInfo3 = Mockito.mock(StepInfo.class);
        String bolt = "Garply";
        JobInfo jobInfo4 = Mockito.mock(JobInfo.class);
        String bookworm = "Waldo";
        StepInfo stepInfo4 = Mockito.mock(StepInfo.class);
        String twilight = "Fred";
        JobInfo jobInfo5 = Mockito.mock(JobInfo.class);
        String tuna = "Plugh";

        doThrow(DefNotFoundException.class).when(converterMap)
                .init(plugins.get());
        when(payload.getStepInfo()).thenReturn(stepInfo).thenReturn(stepInfo2)
                .thenReturn(stepInfo3).thenReturn(stepInfo4);
        when(stepInfo.getStepName()).thenReturn(orange);
        when(payload.getJobInfo()).thenReturn(jobInfo).thenReturn(jobInfo2)
                .thenReturn(jobInfo3).thenReturn(jobInfo4).thenReturn(jobInfo5);
        when(jobInfo.getLabel()).thenReturn(kiwi);
        when(jobInfo2.getGroup()).thenReturn(taskGroup);
        when(jobInfo3.getTask()).thenReturn(taskName);
        when(stepInfo2.getStepName()).thenReturn(cherry);
        when(pluginDef.getPlugins(taskGroup, taskName, stepName))
                .thenReturn(plugins);
        when(converter.convert(cValue)).thenReturn(cValue);
        when(stepInfo3.getStepName()).thenReturn(bolt);
        when(jobInfo4.getLabel()).thenReturn(bookworm);
        when(stepInfo4.getStepName()).thenReturn(twilight);
        when(jobInfo5.getLabel()).thenReturn(tuna);

        assertThrows(StepRunException.class, () -> dataConverter.process());

        verify(data, never()).getItems();
        verify(converterMap, never()).keySet();
        verify(item, never()).getAxisByItemName(itemName);
        verify(axis, never()).getValue();
        verify(converterMap, never()).get(itemName);
        verify(axis, never()).setValue(cValue);
    }
}
