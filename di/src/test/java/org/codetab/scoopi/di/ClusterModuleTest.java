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

public class ClusterModuleTest {

    private static Injector injector;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ClusterModule clusterModule = new ClusterModule();
        injector = Guice.createInjector(clusterModule);

        // init cluster
        IStore store = injector.getInstance(IStore.class);
        ICluster cluster = injector.getInstance(ICluster.class);
        cluster.start("server", null);
        store.open();
        clusterModule.setStore(store);
    }

    @Test
    public void testModuleExtendsBaseModule() {
        assertThat(BaseModule.class.isAssignableFrom(ClusterModule.class))
                .isTrue();
    }

    @Test
    public void testStore() {
        IStore obj1 = injector.getInstance(IStore.class);
        IStore obj2 = injector.getInstance(IStore.class);
        assertThat(obj1)
                .isInstanceOf(org.codetab.scoopi.store.cluster.hz.Store.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testJobStore() {
        IJobStore obj1 = injector.getInstance(IJobStore.class);
        IJobStore obj2 = injector.getInstance(IJobStore.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.cluster.hz.JobStore.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testShutdown() {
        IShutdown obj1 = injector.getInstance(IShutdown.class);
        IShutdown obj2 = injector.getInstance(IShutdown.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.cluster.hz.ClusterShutdown.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testCluster() {
        ICluster obj1 = injector.getInstance(ICluster.class);
        ICluster obj2 = injector.getInstance(ICluster.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.cluster.hz.Cluster.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testBarricade() {
        IBarricade obj1 = injector.getInstance(IBarricade.class);
        IBarricade obj2 = injector.getInstance(IBarricade.class);
        assertThat(obj1).isInstanceOf(
                org.codetab.scoopi.store.cluster.hz.Barricade.class);
        assertThat(obj1).isNotSameAs(obj2);
    }

}
