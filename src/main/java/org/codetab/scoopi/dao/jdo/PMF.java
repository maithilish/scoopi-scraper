package org.codetab.scoopi.dao.jdo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * Singleton which provides JDO PersistenceManagerFactory to DAO layer.
 * Initialize JDO PersistenceManagerFactory from properties file specified by
 * config property gotz.datastore.configFile.
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
    private ConfigService configService;

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
     * by config property gotz.datastore.configFile.
     * @throws CriticalException
     *             if gotz.datastore.configFile config not found or if unable to
     *             read properties file or unable to create persistence manager
     *             factory
     */
    public synchronized void init() {
        if (factory == null) {
            logger.info(Messages.getString("PMF.0")); //$NON-NLS-1$
            String configFile;
            try {
                configFile = Util.join("/", //$NON-NLS-1$
                        configService.getConfig("gotz.datastore.configFile")); // $NON-NLS-2$
            } catch (ConfigNotFoundException e) {
                throw new CriticalException(Messages.getString("PMF.1"), e); //$NON-NLS-1$
            }
            try (InputStream propStream = ioHelper.getInputStream(configFile)) {
                jdoProperties.load(propStream);
                factory = JDOHelper.getPersistenceManagerFactory(jdoProperties);

                logger.info(Messages.getString("PMF.2")); //$NON-NLS-1$
                logger.debug(Messages.getString("PMF.3"), //$NON-NLS-1$
                        Util.getPropertiesAsString(jdoProperties));
                logger.debug(Messages.getString("PMF.4")); //$NON-NLS-1$
            } catch (IOException e) {
                throw new CriticalException(Messages.getString("PMF.5"), e); //$NON-NLS-1$
            }
        }
    }

}
