package org.codetab.scoopi.itest.fin;

import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.defs.yml.Def;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.InitModule;
import org.junit.Before;
import org.junit.Test;

public class DefReferenceIT {

    private String defDir = "/defs/reference";

    private DInjector di;
    private Def def;

    private ConfigService configService;

    @Before
    public void setUp() {
        di = new DInjector(new InitModule());

        configService = di.instance(ConfigService.class);
        configService.init("scoopi.properties", "scoopi-default.xml");

        def = di.instance(Def.class);
    }

    @Test
    public void defReferenceTest() {
        System.setProperty("scoopi.defs.dir", defDir);
        def.init();
    }

}
