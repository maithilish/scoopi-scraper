package org.codetab.scoopi.defs.yml;

import javax.inject.Inject;

import org.codetab.scoopi.defs.ILocatorDefs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.shared.ConfigService;

public class DefsApp {

    @Inject
    private ConfigService configService;

    public static void main(final String[] args) {
        DInjector di = new DInjector();
        DefsApp app = di.instance(DefsApp.class);
        app.start(di);
    }

    public void start(final DInjector di) {

        Defs defs = di.instance(Defs.class);

        configService.init("scoopi-dev.properties", "scoopi-default.xml");

        defs.init();
        defs.initDefProviders();

        ILocatorDefs lp = di.instance(ILocatorDefs.class);
        System.out.println(lp.getGroups());
        System.out.println(lp.getLocatorGroup("snapshot"));
    }

}
