package org.codetab.scoopi.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Date;

import org.codetab.scoopi.store.IStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DInjectorTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDInjectorAbstractModule() {
        SoloModule module = new SoloModule();
        DInjector dInjector = new DInjector(module);
        IStore obj1 = dInjector.instance(IStore.class);
        IStore obj2 = dInjector.instance(IStore.class);
        assertThat(obj1)
                .isInstanceOf(org.codetab.scoopi.store.solo.simple.Store.class);
        assertThat(obj1).isSameAs(obj2);
    }

    @Test
    public void testDInjectorInjector() {
        SoloModule module = new SoloModule();
        DInjector dInjector1 = new DInjector(module);
        DInjector dInjector2 = dInjector1.instance(DInjector.class);

        IStore di1Obj1 = dInjector1.instance(IStore.class);
        IStore di2Obj1 = dInjector2.instance(IStore.class);
        IStore di2Obj2 = dInjector2.instance(IStore.class);
        assertThat(di2Obj1)
                .isInstanceOf(org.codetab.scoopi.store.solo.simple.Store.class);
        assertThat(di2Obj1).isSameAs(di2Obj2);
        assertThat(di1Obj1).isSameAs(di2Obj1);
    }

    @Test
    public void testInstanceClassOfT() {
        SoloModule module = new SoloModule();
        DInjector dInjector = new DInjector(module);
        IStore obj = dInjector.instance(IStore.class);
        assertThat(obj)
                .isInstanceOf(org.codetab.scoopi.store.solo.simple.Store.class);
    }

    @Test
    public void testInstanceStringClassOfT() throws ClassNotFoundException {
        SoloModule module = new SoloModule();
        DInjector dInjector = new DInjector(module);
        Date obj = dInjector.instance("java.util.Date", Date.class);
        assertThat(obj).isInstanceOf(java.util.Date.class);
    }

    @Test
    public void testInstanceStringClassOfTException()
            throws ClassNotFoundException {
        SoloModule module = new SoloModule();
        DInjector dInjector = new DInjector(module);
        assertThrows(ClassNotFoundException.class,
                () -> dInjector.instance("java.util.Xyz", Date.class));
    }
}
