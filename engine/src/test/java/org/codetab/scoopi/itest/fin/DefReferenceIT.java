package org.codetab.scoopi.itest.fin;

import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.InitModule;
import org.junit.Test;

public class DefReferenceIT {

    private String defDir = "/defs/reference";

    @Test
    public void defReferenceTest() {
        System.setProperty("scoopi.defs.dir", defDir);
        System.setProperty("scoopi.propertyFile", "scoopi-test.properties");
        DInjector initInjector =
                new DInjector(new InitModule()).instance(DInjector.class);
        Bootstrap bootstrap = initInjector.instance(Bootstrap.class);
        bootstrap.init();
        bootstrap.start(); // init defs
    }

}
