package org.codetab.scoopi.bootstrap;

import javax.inject.Inject;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.config.ProvidedProperties;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigBootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(ConfigBootstrap.class);

    @Inject
    private Configs configs;
    @Inject
    private ProvidedProperties providedProperties;

    public boolean bootstrap(final IStore store) {
        LOGGER.info("bootstrap config service");

        String defaultConfigFile = "scoopi-default.xml"; //$NON-NLS-1$
        String userConfigFile = providedProperties.getFileName();
        configs.initConfigService(userConfigFile, defaultConfigFile);

        LOGGER.info("rundate {}", configs.getRunDate());
        LOGGER.info(configs.getStage());

        store.put("configService", configs);

        return true;
    }

}
