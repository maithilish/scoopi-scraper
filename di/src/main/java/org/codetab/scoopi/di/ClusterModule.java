package org.codetab.scoopi.di;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.defs.yml.DataDefDef;
import org.codetab.scoopi.defs.yml.DataDefDefData;
import org.codetab.scoopi.defs.yml.ItemDef;
import org.codetab.scoopi.defs.yml.ItemDefData;
import org.codetab.scoopi.defs.yml.LocatorDef;
import org.codetab.scoopi.defs.yml.LocatorDefData;
import org.codetab.scoopi.defs.yml.PluginDef;
import org.codetab.scoopi.defs.yml.PluginDefData;
import org.codetab.scoopi.defs.yml.TaskDef;
import org.codetab.scoopi.defs.yml.TaskDefData;
import org.codetab.scoopi.metrics.IMetricsServer;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.solo.simple.PayloadStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ClusterModule extends AbstractModule {

    private IStore store;

    // private ICluster cluster;

    public ClusterModule(final IStore store) {
        this.store = store;
        // this.cluster = cluster;
    }

    @Override
    protected void configure() {
        bind(IPayloadStore.class).to(PayloadStore.class).in(Singleton.class);
        bind(IMetricsServer.class)
                .to(org.codetab.scoopi.metrics.server.MetricsServer.class)
                .in(Singleton.class);

        // bind cluster specific classes
        bind(IJobStore.class)
                .to(org.codetab.scoopi.store.cluster.hz.JobStore.class)
                .in(Singleton.class);
        bind(ICluster.class)
                .to(org.codetab.scoopi.store.cluster.hz.Cluster.class)
                .in(Singleton.class);
        bind(IShutdown.class)
                .to(org.codetab.scoopi.store.cluster.hz.Shutdown.class)
                .in(Singleton.class);

        bind(ILocatorDef.class).to(LocatorDef.class).in(Singleton.class);
        bind(IItemDef.class).to(ItemDef.class).in(Singleton.class);
        bind(ITaskDef.class).to(TaskDef.class).in(Singleton.class);
        bind(IPluginDef.class).to(PluginDef.class).in(Singleton.class);
        bind(IDataDefDef.class).to(DataDefDef.class).in(Singleton.class);

        // factory to create instances with constructor parameters
        install(new FactoryModuleBuilder().build(BasicFactory.class));
    }

    // @Provides
    // @Singleton
    // ICluster provideCluster() {
    // return cluster;
    // }

    @Provides
    @Singleton
    Configs provideConfigService() {
        return (Configs) store.get("configs");
    }

    @Provides
    @Singleton
    LocatorDefData provideLocatorDef() {
        return (LocatorDefData) SerializationUtils
                .deserialize((byte[]) store.get("locatorDef"));
    }

    @Provides
    @Singleton
    TaskDefData provideTaskDefData() {
        return (TaskDefData) SerializationUtils
                .deserialize((byte[]) store.get("taskDef"));
    }

    @Provides
    @Singleton
    ItemDefData provideItemDefData() {
        return (ItemDefData) SerializationUtils
                .deserialize((byte[]) store.get("itemDef"));
    }

    @Provides
    @Singleton
    PluginDefData providePluginDefData() {
        return (PluginDefData) SerializationUtils
                .deserialize((byte[]) store.get("pluginDef"));
    }

    @Provides
    @Singleton
    DataDefDefData provideDataDefDefData() {
        return (DataDefDefData) SerializationUtils
                .deserialize((byte[]) store.get("dataDefDef"));
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
