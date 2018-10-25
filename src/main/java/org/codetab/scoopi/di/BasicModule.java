package org.codetab.scoopi.di;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import javax.inject.Singleton;

import org.codetab.scoopi.defs.IAxisDefs;
import org.codetab.scoopi.defs.IDataDefDefs;
import org.codetab.scoopi.defs.IItemDefs;
import org.codetab.scoopi.defs.ILocatorDefs;
import org.codetab.scoopi.defs.IPluginDefs;
import org.codetab.scoopi.defs.ITaskDefs;
import org.codetab.scoopi.defs.yml.AxisDefs;
import org.codetab.scoopi.defs.yml.DataDefDefs;
import org.codetab.scoopi.defs.yml.ItemDefs;
import org.codetab.scoopi.defs.yml.LocatorDefs;
import org.codetab.scoopi.defs.yml.PluginDefs;
import org.codetab.scoopi.defs.yml.TaskDefs;
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

        // bind basic store
        bind(IStore.class).to(BasicStore.class).in(Singleton.class);

        // bind yaml defs
        bind(ILocatorDefs.class).to(LocatorDefs.class).in(Singleton.class);
        bind(ITaskDefs.class).to(TaskDefs.class).in(Singleton.class);
        bind(IDataDefDefs.class).to(DataDefDefs.class).in(Singleton.class);
        bind(IAxisDefs.class).to(AxisDefs.class).in(Singleton.class);
        bind(IItemDefs.class).to(ItemDefs.class).in(Singleton.class);
        bind(IPluginDefs.class).to(PluginDefs.class).in(Singleton.class);

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
