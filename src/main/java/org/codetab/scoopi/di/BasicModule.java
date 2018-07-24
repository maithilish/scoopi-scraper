package org.codetab.scoopi.di;

import javax.inject.Singleton;

import org.codetab.scoopi.defs.IDataDefProvider;
import org.codetab.scoopi.defs.ILocatorProvider;
import org.codetab.scoopi.defs.ITaskProvider;
import org.codetab.scoopi.defs.yml.DataDefProvider;
import org.codetab.scoopi.defs.yml.LocatorProvider;
import org.codetab.scoopi.defs.yml.TaskProvider;
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

        // bind yaml defs providers
        bind(ILocatorProvider.class).to(LocatorProvider.class)
                .in(Singleton.class);
        bind(ITaskProvider.class).to(TaskProvider.class).in(Singleton.class);
        bind(IDataDefProvider.class).to(DataDefProvider.class)
                .in(Singleton.class);

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

}
