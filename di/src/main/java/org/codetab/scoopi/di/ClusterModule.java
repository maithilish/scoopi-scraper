package org.codetab.scoopi.di;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.cluster.ICluster;
import org.codetab.scoopi.store.cluster.hz.JobStore;
import org.codetab.scoopi.store.solo.simple.PayloadStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ClusterModule extends AbstractModule {

    private IStore store;

    private ICluster cluster;

    public ClusterModule(final IStore store, final ICluster cluster) {
        this.store = store;
        this.cluster = cluster;
    }

    @Override
    protected void configure() {
        // bind basic store
        bind(IPayloadStore.class).to(PayloadStore.class).in(Singleton.class);
        bind(IJobStore.class).to(JobStore.class).in(Singleton.class);

        // factory to create instances with constructor parameters
        install(new FactoryModuleBuilder().build(BasicFactory.class));
    }

    @Provides
    @Singleton
    ICluster provideCluster() {
        return cluster;
    }

    @Provides
    @Singleton
    Configs provideConfigService() {
        return (Configs) store.get("configs");
    }

    @Provides
    @Singleton
    ILocatorDef provideLocatorDef() {
        return (ILocatorDef) store.get("locatorDef");
    }

    @Provides
    @Singleton
    ITaskDef provideTaskDef() {
        return (ITaskDef) store.get("taskDef");
    }

    @Provides
    @Singleton
    IDataDefDef provideDataDefDef() {
        return (IDataDefDef) store.get("dataDefDef");
    }

    @Provides
    @Singleton
    IItemDef provideItemDef() {
        return (IItemDef) store.get("itemDef");
    }

    @Provides
    @Singleton
    IPluginDef providePluginDef() {
        return (IPluginDef) store.get("pluginDef");
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
