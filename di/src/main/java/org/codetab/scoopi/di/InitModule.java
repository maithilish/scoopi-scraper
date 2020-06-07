package org.codetab.scoopi.di;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IItemDefBuilder;
import org.codetab.scoopi.defs.ILocatorDefBuilder;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDefBuilder;
import org.codetab.scoopi.defs.yml.DataDefDef;
import org.codetab.scoopi.defs.yml.Def;
import org.codetab.scoopi.defs.yml.ItemDefBuilder;
import org.codetab.scoopi.defs.yml.LocatorDefBuilder;
import org.codetab.scoopi.defs.yml.PluginDef;
import org.codetab.scoopi.defs.yml.TaskDefBuilder;
import org.codetab.scoopi.store.cluster.IClusterStore;
import org.codetab.scoopi.store.solo.ISoloStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class InitModule extends AbstractModule {

    @Override
    protected void configure() {
        // bind cluster, store to hazelcast
        bind(IClusterStore.class)
                .to(org.codetab.scoopi.store.cluster.hz.Store.class)
                .in(Singleton.class);

        // bind solo to simple store
        bind(ISoloStore.class)
                .to(org.codetab.scoopi.store.solo.simple.Store.class)
                .in(Singleton.class);

        // FIXME - remove these except builders
        // bind yaml defs
        bind(Configs.class).in(Singleton.class);
        bind(IDef.class).to(Def.class).in(Singleton.class);
        // bind(ILocatorDef.class).to(LocatorDef.class).in(Singleton.class);
        // bind(ITaskDef.class).to(TaskDef.class).in(Singleton.class);
        // bind(IItemDef.class).to(ItemDef.class).in(Singleton.class);
        bind(IDataDefDef.class).to(DataDefDef.class).in(Singleton.class);
        bind(IPluginDef.class).to(PluginDef.class).in(Singleton.class);

        bind(ILocatorDefBuilder.class).to(LocatorDefBuilder.class)
                .in(Singleton.class);
        bind(ITaskDefBuilder.class).to(TaskDefBuilder.class)
                .in(Singleton.class);
        bind(IItemDefBuilder.class).to(ItemDefBuilder.class)
                .in(Singleton.class);

        // bind basic store
        // bind(IPayloadStore.class).to(PayloadStore.class);

        // factory to create instances with constructor parameters
        install(new FactoryModuleBuilder().build(BasicFactory.class));
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