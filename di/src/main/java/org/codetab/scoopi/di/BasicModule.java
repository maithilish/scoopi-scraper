package org.codetab.scoopi.di;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.defs.IDef;
import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.defs.ITaskDef;
import org.codetab.scoopi.defs.yml.DataDefDef;
import org.codetab.scoopi.defs.yml.Def;
import org.codetab.scoopi.defs.yml.ItemDef;
import org.codetab.scoopi.defs.yml.LocatorDef;
import org.codetab.scoopi.defs.yml.PluginDef;
import org.codetab.scoopi.defs.yml.TaskDef;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.basic.BasicStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class BasicModule extends AbstractModule {

    @Override
    protected void configure() {

        // bind yaml defs
        bind(IDef.class).to(Def.class).in(Singleton.class);
        bind(ILocatorDef.class).to(LocatorDef.class).in(Singleton.class);
        bind(ITaskDef.class).to(TaskDef.class).in(Singleton.class);
        bind(IDataDefDef.class).to(DataDefDef.class).in(Singleton.class);
        bind(IItemDef.class).to(ItemDef.class).in(Singleton.class);
        bind(IPluginDef.class).to(PluginDef.class).in(Singleton.class);

        // bind basic store
        bind(IStore.class).to(BasicStore.class).in(Singleton.class);

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
        return (OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
    }

    @Provides
    RuntimeMXBean getRuntimeMxBean() {
        return (RuntimeMXBean) ManagementFactory.getRuntimeMXBean();
    }

}
