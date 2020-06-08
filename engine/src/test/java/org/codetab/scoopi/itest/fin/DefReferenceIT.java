package org.codetab.scoopi.itest.fin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.bootstrap.ConfigsComposer;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.store.IStore;
import org.junit.Test;

public class DefReferenceIT {

    @Test
    public void defReferenceTest() throws ConfigNotFoundException {

        String defDir = "/defs/reference";

        System.setProperty("scoopi.defs.dir", defDir);
        System.setProperty("scoopi.propertyFile", "scoopi-test.properties");
        System.setProperty("scoopi.cluster.enable", "false");

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.boot();
        DInjector di = bootstrap.getdInjector();
        ConfigsComposer configBooter = di.instance(ConfigsComposer.class);
        configBooter.compose();

        IStore store = di.instance(IStore.class);
        Properties properties = (Properties) store.get("configs");
        String actual = properties.getProperty("scoopi.defs.dir");

        assertThat(actual).isEqualTo(defDir);
    }

}
