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
import org.codetab.scoopi.store.IPayloadStore;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.local.simple.PayloadStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class RunModule extends AbstractModule {

    private IStore store;

    public RunModule(IStore store) {
        this.store = store;
    }

    @Override
    protected void configure() {
        // bind basic store
        bind(IPayloadStore.class).to(PayloadStore.class).in(Singleton.class);

        // factory to create instances with constructor parameters
        install(new FactoryModuleBuilder().build(BasicFactory.class));
    }

    @Provides
    @Singleton
    Configs provideConfigService() {
        System.out.println("configService provider: " + store.getName());
        return (Configs) store.get("configService");
    }

    @Provides
    @Singleton
    ILocatorDef provideLocatorDef() {
        System.out.println("locatorDef provider: " + store.getName());
        return (ILocatorDef) store.get("locatorDef");
    }

    @Provides
    @Singleton
    ITaskDef provideTaskDef() {
        System.out.println("taskDef provider: " + store.getName());
        return (ITaskDef) store.get("taskDef");
    }

    @Provides
    @Singleton
    IDataDefDef provideDataDefDef() {
        System.out.println("dataDefDef provider: " + store.getName());
        return (IDataDefDef) store.get("dataDefDef");
    }

    @Provides
    @Singleton
    IItemDef provideItemDef() {
        System.out.println("itemDef provider: " + store.getName());
        return (IItemDef) store.get("itemDef");
    }

    @Provides
    @Singleton
    IPluginDef providePluginDef() {
        System.out.println("pluginDef provider: " + store.getName());
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
        return (OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
    }

    @Provides
    RuntimeMXBean getRuntimeMxBean() {
        return (RuntimeMXBean) ManagementFactory.getRuntimeMXBean();
    }
}
