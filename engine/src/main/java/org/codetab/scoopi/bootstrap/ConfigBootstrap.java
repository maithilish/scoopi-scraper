package org.codetab.scoopi.bootstrap;

import javax.inject.Inject;

import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.config.ProvidedProperties;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigBootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(ConfigBootstrap.class);

    @Inject
    private ConfigService configService;
    @Inject
    private ProvidedProperties providedProperties;

    public boolean bootstrap(final IStore store) {
        LOGGER.info("bootstrap config service");

        String defaultConfigFile = "scoopi-default.xml"; //$NON-NLS-1$
        String userConfigFile = providedProperties.getFileName();
        configService.init(userConfigFile, defaultConfigFile);

        LOGGER.info("rundate {}", configService.getRunDate());
        LOGGER.info(configService.getStage());

        store.put("configService", configService);

        return true;
    }

}
