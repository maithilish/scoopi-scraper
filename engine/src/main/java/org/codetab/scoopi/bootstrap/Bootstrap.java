package org.codetab.scoopi.bootstrap;

import javax.inject.Inject;

import org.codetab.scoopi.config.BootstrapConfigs;
import org.codetab.scoopi.di.DInjector;
import org.codetab.scoopi.di.RunModule;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.store.IStore;
import org.codetab.scoopi.store.cluster.IClusterStore;
import org.codetab.scoopi.store.local.ILocalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

    static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    @Inject
    private BootstrapConfigs bootstrapConfigs;
    @Inject
    private DInjector initInjector;
    @Inject
    private ConfigBootstrap configBootstrap;
    @Inject
    private DefBootstrap defBootstrap;
    @Inject
    private ErrorLogger errorLogger;

    private IStore store;
    private DInjector dInjector; // actual injector to run scoopi

    public void init() {
        if (bootstrapConfigs.isSolo()) {
            LOGGER.info("Scoopi [solo/cluster]: solo"); //$NON-NLS-1$
            store = initInjector.instance(ILocalStore.class);
            dInjector = new DInjector(new RunModule(store))
                    .instance(DInjector.class);
        } else {
            LOGGER.info("Scoopi [solo/cluster]: cluster"); //$NON-NLS-1$
            store = initInjector.instance(IClusterStore.class);
            dInjector = new DInjector(new RunModule(store))
                    .instance(DInjector.class);
        }
    }

    public void start() {
        try {
            LOGGER.info("Bootsrap scoopi ..."); //$NON-NLS-1$

            configBootstrap.bootstrap(store);
            defBootstrap.bootstrap(store);
            defBootstrap.updateDataDefs(store);

        } catch (CriticalException e) {
            String message = "terminate scoopi";
            errorLogger.log(CAT.FATAL, message, e);
        } finally {
            LOGGER.info("scoopi store ready");
        }
    }

    public DInjector getdInjector() {
        return dInjector;
    }

    public IStore getStore() {
        return store;
    }

}
