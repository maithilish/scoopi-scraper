package org.codetab.scoopi.daomig.jdo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * Singleton which provides JDO PersistenceManagerFactory to DAO layer.
 * Initialize JDO PersistenceManagerFactory from properties file specified by
 * config property scoopi.datastore.configFile.
 * @author Maithilish
 *
 */
@ThreadSafe
@Singleton
public class PMF {

    /**
     * logger.
     */
    private final Logger logger = LoggerFactory.getLogger(PMF.class);

    /**
     * ConfigService.
     */
    @Inject
    private Configs configs;

    /**
     * ResourceStream.
     */
    @Inject
    private IOHelper ioHelper;
    /**
     * JDO properties.
     */
    @Inject
    private Properties jdoProperties;

    /**
     * JDO PersistenceManagerFactory.
     */
    @GuardedBy("this")
    private PersistenceManagerFactory factory;

    /**
     * <p>
     * Private Constructor - Singleton.
     */
    @Inject
    private PMF() {
    }

    /**
     * <p>
     * Get JDO PersistenceManagerFactory.
     * @return persistence manager factory
     */
    public PersistenceManagerFactory getFactory() {
        return factory;
    }

    /**
     * <p>
     * Initialize JDO PersistenceManagerFactory from properties file specified
     * by config property scoopi.datastore.configFile.
     * @throws CriticalException
     *             if scoopi.datastore.configFile config not found or if unable
     *             to read properties file or unable to create persistence
     *             manager factory
     */
    public synchronized void init() {
        if (factory == null) {
            try {
                logger.info("initalize JDO PMF");
                String configFile = String.join("", "/",
                        configs.getConfig("scoopi.datastore.configFile"));
                try (InputStream propStream =
                        ioHelper.getInputStream(configFile)) {

                    jdoProperties.load(propStream);
                    factory = JDOHelper
                            .getPersistenceManagerFactory(jdoProperties);

                    logger.info(" initialized JDO PMF");
                    logger.debug("PMF properties {}",
                            Util.getPropertiesAsString(jdoProperties));
                } catch (IOException e) {
                    throw e;
                }
            } catch (ConfigNotFoundException | IOException e) {
                throw new CriticalException("unable to initialize JDO PMF", e); //$NON-NLS-1$
            }
        }
    }
}
