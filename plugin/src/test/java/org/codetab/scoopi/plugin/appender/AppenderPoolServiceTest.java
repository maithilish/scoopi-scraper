package org.codetab.scoopi.plugin.appender;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.SoloModule;
import org.codetab.scoopi.pool.PoolService;
import org.codetab.scoopi.store.IStore;
import org.junit.Before;
import org.junit.Test;

public class AppenderPoolServiceTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {

        SoloModule module = new SoloModule();
        DInjector dInjector = new DInjector(module);
        IStore store = dInjector.instance(IStore.class);
        store.open();
        store.put("config", new Properties());
        module.setStore(store);

        AppenderPoolService aps1 =
                dInjector.instance(AppenderPoolService.class);
        AppenderPoolService aps2 =
                dInjector.instance(AppenderPoolService.class);

        assertTrue(PoolService.class.isAssignableFrom(aps1.getClass()));

        assertSame(aps1, aps2);

    }

}
