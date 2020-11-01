package org.codetab.scoopi.bootstrap;

import java.util.Properties;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.ConfigBuilder;
import org.codetab.scoopi.config.PropertyFiles;
import org.codetab.scoopi.store.IStore;

public class ConfigsComposer {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ConfigBuilder configBuilder;
    @Inject
    private PropertyFiles propertyFiles;
    @Inject
    private IStore store;

    public boolean compose() {
        LOG.info("bootstrap config and push to store");

        String defaultConfigFile = "scoopi-default.xml"; //$NON-NLS-1$
        String userConfigFile = propertyFiles.getFileName();
        configBuilder.build(userConfigFile, defaultConfigFile);

        Properties properties = configBuilder.getEffectiveProperties();
        store.put("configs", properties);

        configBuilder.logConfigs(properties);

        return true;
    }

}
