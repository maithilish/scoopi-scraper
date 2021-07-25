package org.codetab.scoopi.di;

import static org.assertj.core.api.Assertions.assertThat;

import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.codetab.scoopi.store.IStore;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SoloModuleTest {

    private static Injector injector;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        injector = Guice.createInjector(new SoloModule());
    }

    @Test
    public void testModuleExtendsBaseModule() {
        assertThat(BaseModule.class.isAssignableFrom(SoloModule.class))
                .isTrue();
    }

    @Test
    public void testStore() {
        IStore obj1 = injector.getInstance(IStore.class);
        IStore obj2 = injector.getInstance(IStore.class);
        assertThat(obj1)
                .isInstanceOf(org.codetab.scoopi.store.solo.simple.Store.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testJobStore() {
        IJobStore obj1 = injector.getInstance(IJobStore.class);
        IJobStore obj2 = injector.getInstance(IJobStore.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.solo.simple.JobStore.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testShutdown() {
        IShutdown obj1 = injector.getInstance(IShutdown.class);
        IShutdown obj2 = injector.getInstance(IShutdown.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.solo.simple.SoloShutdown.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testCluster() {
        ICluster obj1 = injector.getInstance(ICluster.class);
        ICluster obj2 = injector.getInstance(ICluster.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.solo.simple.SoloCluster.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testBarricade() {
        IBarricade obj1 = injector.getInstance(IBarricade.class);
        IBarricade obj2 = injector.getInstance(IBarricade.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.solo.simple.Barricade.class);
        assertThat(obj1).isNotSameAs(obj2);
    }
}
