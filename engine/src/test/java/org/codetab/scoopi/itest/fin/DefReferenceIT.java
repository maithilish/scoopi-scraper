package org.codetab.scoopi.itest.fin;

import org.codetab.scoopi.bootstrap.Bootstrap;
import org.codetab.scoopi.config.BootConfigs;
import org.junit.Test;

public class DefReferenceIT {

    @Test
    public void defReferenceTest() {

        String defDir = "/defs/reference";

        System.setProperty("scoopi.defs.dir", defDir);
        System.setProperty("scoopi.propertyFile", "scoopi-test.properties");
        System.setProperty("scoopi.cluster.enable", "false");

        BootConfigs bootConfigs = new BootConfigs();
        bootConfigs.configureLogPath();
        Bootstrap bootstrap = new Bootstrap(bootConfigs);
        bootstrap.bootDi();

        bootstrap.bootCluster();
        bootstrap.waitForQuorum();

        // compose reference defs
        bootstrap.setup();
    }

}
