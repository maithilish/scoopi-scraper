package org.codetab.scoopi.dao.jdo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.codetab.scoopi.dao.IDaoUtil;
import org.codetab.scoopi.defs.yml.Defs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.shared.ConfigService;
import org.junit.BeforeClass;

public abstract class ITBase {

    protected static DInjector di;
    protected static IDaoUtil daoUtil;
    protected static HashSet<String> schemaClasses;
    protected static Defs defs;
    protected static ObjectFactory objectFactory;
    protected static ConfigService configService;

    @BeforeClass
    public static void setUpBeforeClass()
            throws IOException, IllegalAccessException, URISyntaxException {
        di = new DInjector();

        configService = di.instance(ConfigService.class);
        configService.init("scoopi.properties", "scoopi-default.xml");
        configService.getConfigs().setProperty("scoopi.useDatastore", "true");
        configService.getConfigs().setProperty("scoopi.defs.dir",
                "/testdefs/test-1");

        schemaClasses = new HashSet<>();

        daoUtil = new JdoDaoUtilFactory(di).getUtilDao();

        defs = di.instance(Defs.class);
        defs.init();
        defs.initDefProviders();

        objectFactory = di.instance(ObjectFactory.class);
    }

}
