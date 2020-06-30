package org.codetab.scoopi.di;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Properties;

import javax.inject.Singleton;

import org.codetab.scoopi.config.ConfigProperties;
import org.codetab.scoopi.dao.ILocatorDao;
import org.codetab.scoopi.dao.IMetadataDao;
import org.codetab.scoopi.dao.fs.LocatorDao;
import org.codetab.scoopi.dao.fs.MetadataDao;
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
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.solo.simple.PayloadStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public abstract class BaseModule extends AbstractModule {

    private IStore store;

    public BaseModule() {
        super();
    }

    @Override
    protected void configure() {

        bind(IPayloadStore.class).to(PayloadStore.class).in(Singleton.class);
        bind(IMetricsServer.class)
                .to(org.codetab.scoopi.metrics.server.MetricsServer.class)
                .in(Singleton.class);

        bind(IDef.class).to(Def.class).in(Singleton.class);
        bind(ILocatorDef.class).to(LocatorDef.class).in(Singleton.class);
        bind(IItemDef.class).to(ItemDef.class).in(Singleton.class);
        bind(ITaskDef.class).to(TaskDef.class).in(Singleton.class);
        bind(IPluginDef.class).to(PluginDef.class).in(Singleton.class);
        bind(IDataDefDef.class).to(DataDefDef.class).in(Singleton.class);

        bind(ILocatorDao.class).to(LocatorDao.class).in(Singleton.class);
        bind(IMetadataDao.class).to(MetadataDao.class).in(Singleton.class);

        // factory to create instances with constructor parameters
        install(new FactoryModuleBuilder().build(BasicFactory.class));
    }

    public void setStore(final IStore store) {
        this.store = store;
    }

    @Provides
    @Singleton
    ConfigProperties provideConfigProperties() {
        return new ConfigProperties((Properties) store.get("configs"));
    }

    @Provides
    @Singleton
    LocatorDefData provideLocatorDef() {
        return (LocatorDefData) store.get("locatorDef");
    }

    @Provides
    @Singleton
    TaskDefData provideTaskDefData() {
        return (TaskDefData) store.get("taskDef");
    }

    @Provides
    @Singleton
    ItemDefData provideItemDefData() {
        return (ItemDefData) store.get("itemDef");
    }

    @Provides
    @Singleton
    PluginDefData providePluginDefData() {
        return (PluginDefData) store.get("pluginDef");
    }

    @Provides
    @Singleton
    DataDefDefData provideDataDefDefData() {
        return (DataDefDefData) store.get("dataDefDef");
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }

    @Provides
    Runtime provideRuntime() {
        return Runtime.getRuntime();
    }

    @Provides
    OperatingSystemMXBean getOsMxBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    @Provides
    RuntimeMXBean getRuntimeMxBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

}
