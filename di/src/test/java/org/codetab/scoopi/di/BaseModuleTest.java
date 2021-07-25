package org.codetab.scoopi.di;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.inject.Inject;

import org.codetab.scoopi.config.ConfigProperties;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.dao.fs.DataDao;
import org.codetab.scoopi.dao.fs.DocumentDao;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.defs.yml.DataDefDef;
import org.codetab.scoopi.defs.yml.DataDefDefData;
import org.codetab.scoopi.defs.yml.Def;
import org.codetab.scoopi.defs.yml.ItemDef;
import org.codetab.scoopi.defs.yml.ItemDefData;
import org.codetab.scoopi.defs.yml.LocatorDef;
import org.codetab.scoopi.defs.yml.LocatorDefData;
import org.codetab.scoopi.defs.yml.PluginDef;
import org.codetab.scoopi.defs.yml.PluginDefData;
import org.codetab.scoopi.defs.yml.TaskDef;
import org.codetab.scoopi.defs.yml.TaskDefData;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.metrics.PoolStat;
import org.codetab.scoopi.metrics.server.MetricsServer;
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.solo.simple.PayloadStore;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BaseModuleTest {

    private static Injector injector;
    private static LocatorDefData locatorDefData;
    private static TaskDefData taskDefData;
    private static ItemDefData itemDefData;
    private static Properties configs;

    private static SoloModule module;
    private static PluginDefData pluginDefData;
    private static DataDefDefData dataDefDefData;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        module = new SoloModule();
        injector = Guice.createInjector(module);

        IStore store = injector.getInstance(IStore.class);
        store.open();
        module.setStore(store);

        configs = new Properties();
        store.put("configs", configs);

        locatorDefData = new LocatorDefData();
        store.put("locatorDef", locatorDefData);

        taskDefData = new TaskDefData();
        store.put("taskDef", taskDefData);

        itemDefData = new ItemDefData();
        store.put("itemDef", itemDefData);

        pluginDefData = new PluginDefData();
        store.put("pluginDef", pluginDefData);

        dataDefDefData = new DataDefDefData();
        store.put("dataDefDef", dataDefDefData);
    }

    @Test
    public void testPlayloadStore() {
        IPayloadStore obj1 = injector.getInstance(IPayloadStore.class);
        IPayloadStore obj2 = injector.getInstance(IPayloadStore.class);
        assertThat(obj1).isInstanceOf(PayloadStore.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testMetricsServer() {
        IMetricsServer obj1 = injector.getInstance(IMetricsServer.class);
        IMetricsServer obj2 = injector.getInstance(IMetricsServer.class);
        assertThat(obj1).isInstanceOf(MetricsServer.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testDef() {
        IDef obj1 = injector.getInstance(IDef.class);
        IDef obj2 = injector.getInstance(IDef.class);
        assertThat(obj1).isInstanceOf(Def.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testLocatorDef() {
        ILocatorDef obj1 = injector.getInstance(ILocatorDef.class);
        ILocatorDef obj2 = injector.getInstance(ILocatorDef.class);
        assertThat(obj1).isInstanceOf(LocatorDef.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testItemDef() {
        IItemDef obj1 = injector.getInstance(IItemDef.class);
        IItemDef obj2 = injector.getInstance(IItemDef.class);
        assertThat(obj1).isInstanceOf(ItemDef.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testTaskDef() {
        ITaskDef obj1 = injector.getInstance(ITaskDef.class);
        ITaskDef obj2 = injector.getInstance(ITaskDef.class);
        assertThat(obj1).isInstanceOf(TaskDef.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testPluginDef() {
        IPluginDef obj1 = injector.getInstance(IPluginDef.class);
        IPluginDef obj2 = injector.getInstance(IPluginDef.class);
        assertThat(obj1).isInstanceOf(PluginDef.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testDataDefDef() {
        IDataDefDef obj1 = injector.getInstance(IDataDefDef.class);
        IDataDefDef obj2 = injector.getInstance(IDataDefDef.class);
        assertThat(obj1).isInstanceOf(DataDefDef.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testDocumentDao() {
        IDocumentDao obj1 = injector.getInstance(IDocumentDao.class);
        IDocumentDao obj2 = injector.getInstance(IDocumentDao.class);
        assertThat(obj1).isInstanceOf(DocumentDao.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testDataDao() {
        IDataDao obj1 = injector.getInstance(IDataDao.class);
        IDataDao obj2 = injector.getInstance(IDataDao.class);
        assertThat(obj1).isInstanceOf(DataDao.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testBasicFactory() {
        BasicFactory factory = injector.getInstance(BasicFactory.class);
        PoolStat obj1 = factory.getPoolStat();
        PoolStat obj2 = factory.getPoolStat();
        assertThat(obj1).isInstanceOf(PoolStat.class);
        assertThat(obj1).isNotSameAs(obj2);
    }

    @Test
    public void testProvideConfigProperties() throws ConfigNotFoundException {
        configs.setProperty("testKey", "testValue");
        ConfigProperties obj1 = injector.getInstance(ConfigProperties.class);
        ConfigProperties obj2 = injector.getInstance(ConfigProperties.class);
        assertThat(obj1).isInstanceOf(ConfigProperties.class);
        assertThat(obj1).isSameAs(obj2);
        assertThat(obj1.getConfig("testKey")).isEqualTo("testValue");
    }

    @Test
    public void testProvideLocatorDef() {
        // test @Provides exists
        LocatorDefData provided = injector.getInstance(LocatorDefData.class);
        assertThat(provided).isSameAs(locatorDefData);

        // test provides method
        LocatorDefData provided1 = module.provideLocatorDefData();
        LocatorDefData provided2 = module.provideLocatorDefData();
        assertThat(provided1).isSameAs(provided2);
        assertThat(provided1).isSameAs(locatorDefData);
    }

    @Test
    public void testProvideTaskDefData() {
        // test @Provides exists
        TaskDefData provided = injector.getInstance(TaskDefData.class);
        assertThat(provided).isSameAs(taskDefData);

        // test provides method
        TaskDefData provided1 = module.provideTaskDefData();
        TaskDefData provided2 = module.provideTaskDefData();
        assertThat(provided1).isSameAs(provided2);
        assertThat(provided1).isSameAs(taskDefData);
    }

    @Test
    public void testProvideItemDefData() {
        // test @Provides exists
        ItemDefData provided = injector.getInstance(ItemDefData.class);
        assertThat(provided).isSameAs(itemDefData);

        // test provides method
        ItemDefData provided1 = module.provideItemDefData();
        ItemDefData provided2 = module.provideItemDefData();
        assertThat(provided1).isSameAs(provided2);
        assertThat(provided1).isSameAs(itemDefData);
    }

    @Test
    public void testProvidePluginDefData() {
        // test @Provides exists
        PluginDefData provided = injector.getInstance(PluginDefData.class);
        assertThat(provided).isSameAs(pluginDefData);

        // test provides method
        PluginDefData provided1 = module.providePluginDefData();
        PluginDefData provided2 = module.providePluginDefData();
        assertThat(provided1).isSameAs(provided2);
        assertThat(provided1).isSameAs(pluginDefData);
    }

    @Test
    public void testProvideDataDefDefData() {
        // test @Provides exists
        DataDefDefData provided = injector.getInstance(DataDefDefData.class);
        assertThat(provided).isSameAs(dataDefDefData);

        // test provides method
        DataDefDefData provided1 = module.provideDataDefDefData();
        DataDefDefData provided2 = module.provideDataDefDefData();
        assertThat(provided1).isSameAs(provided2);
        assertThat(provided1).isSameAs(dataDefDefData);
    }

    @Test
    public void testProvideObjectMapper() {
        // test provides method
        ObjectMapper provided = module.provideObjectMapper();
        assertThat(provided.getFactory().getFormatName()).isEqualTo("YAML");

        // test @Singleton exists
        ObjectMapperWrapper wrapper1 =
                injector.getInstance(ObjectMapperWrapper.class);
        ObjectMapperWrapper wrapper2 =
                injector.getInstance(ObjectMapperWrapper.class);
        assertThat(wrapper1.getObjectMapper())
                .isSameAs(wrapper2.getObjectMapper());
    }

    @Test
    public void testProvideRuntime() {
        assertThat(module.provideRuntime()).isSameAs(Runtime.getRuntime());
    }

    @Test
    public void testGetOsMxBean() {
        assertThat(module.getOsMxBean())
                .isSameAs(ManagementFactory.getOperatingSystemMXBean());
    }

    @Test
    public void testGetRuntimeMxBean() {
        assertThat(module.getRuntimeMxBean())
                .isSameAs(ManagementFactory.getRuntimeMXBean());
    }

}

/**
 * To test @Singleton in provides method
 * @author m
 *
 */
class ObjectMapperWrapper {

    @Inject
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
