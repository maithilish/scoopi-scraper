package org.codetab.scoopi.bootstrap;

import java.util.Properties;

import javax.inject.Inject;

import org.codetab.scoopi.config.ConfigBuilder;
import org.codetab.scoopi.config.PropertyFiles;
import org.codetab.scoopi.store.IStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigsComposer {

    static final Logger LOGGER = LoggerFactory.getLogger(ConfigsComposer.class);

    @Inject
    private ConfigBuilder configBuilder;
    @Inject
    private PropertyFiles propertyFiles;
    @Inject
    private IStore store;

    public boolean compose() {
        LOGGER.info("bootstrap config and push to store");

        String defaultConfigFile = "scoopi-default.xml"; //$NON-NLS-1$
        String userConfigFile = propertyFiles.getFileName();
        configBuilder.build(userConfigFile, defaultConfigFile);

        Properties properties = configBuilder.getEffectiveProperties();
        store.put("configs", properties);

        configBuilder.logConfigs(properties);

        return true;
    }

}
