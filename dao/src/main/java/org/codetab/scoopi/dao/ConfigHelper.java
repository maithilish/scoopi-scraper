package org.codetab.scoopi.dao;

import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHelper {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigHelper.class);

    @Inject
    private ConfigService configService;

    public ORM getOrmType() {
        ORM orm = ORM.JDO;
        try {
            String ormName = configService.getConfig("scoopi.datastore.orm"); //$NON-NLS-1$
            if (StringUtils.compareIgnoreCase(ormName, "jdo") == 0) { //$NON-NLS-1$
                orm = ORM.JDO;
            }
            if (StringUtils.compareIgnoreCase(ormName, "jpa") == 0) { //$NON-NLS-1$
                orm = ORM.JPA;
            }
        } catch (ConfigNotFoundException e) {
            LOGGER.warn("{}", e.getMessage());
            LOGGER.debug("", e);
        }
        return orm;
    }

    public Date getRunDateTime() {
        return configService.getRunDateTime();
    }

    /**
     * Return effective persist for a type based on user provided config.
     * <p>
     * User can define persist config scoopi.useDatastore=true|false or for a
     * type as scoopi.persist.locator=true|false in config file
     * scoopi.properties.
     * </p>
     * <p>
     * This method returns boolean for a key and if not found then true;
     * </p>
     * @param key
     *            scoopi.persist.locator|data|datadef
     * @return value of key and if not found then true
     */
    public boolean isPersist(final String configKey) {
        return configService.isPersist(configKey);
    }

    /**
     *
     * <p>
     * Return value for the key scoopi.useDatastore
     * </p>
     * <p>
     * This method returns for the key scoopi.useDatastore and if not found then
     * true;
     * </p>
     * @param key
     *            scoopi.useDatastore
     * @return value of key and if not found then true
     */
    public boolean useDataStore() {
        return configService.getBoolean("scoopi.useDatastore");
    }

}
