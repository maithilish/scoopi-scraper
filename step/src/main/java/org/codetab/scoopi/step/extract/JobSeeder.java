package org.codetab.scoopi.step.extract;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERRORCAT;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;

@Singleton
public class JobSeeder {

    static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private ILocatorDef locatorDef;
    @Inject
    private JobMediator jobMediator;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private PayloadFactory payloadFactory;
    @Inject
    private Errors errors;
    @Inject
    private CrashCleaner crashCleaner;

    private AtomicBoolean seeder = new AtomicBoolean(false);

    public void seedLocatorGroups() {
        seeder.set(true);
        LOG.info("seed defined locator groups");
        final String stepName = "start"; //$NON-NLS-1$
        String seederClzName = null;
        try {
            seederClzName = configs.getConfig("scoopi.seeder.class"); //$NON-NLS-1$
        } catch (final ConfigNotFoundException e) {
            final String message = "unable seed locator group";
            throw new CriticalException(message, e);
        }

        final List<LocatorGroup> locatorGroups = locatorDef.getLocatorGroups();
        jobMediator.setSeedDoneSignal(locatorGroups.size()); // CountDownLatch

        final List<Payload> payloads = payloadFactory
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (final Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (final InterruptedException e) {
                // FIXME - interruptedFix refactor all
                String group = payload.getJobInfo().getGroup();
                errors.inc();
                LOG.error("seed locator group: {} [{}]", group,
                        ERRORCAT.INTERNAL, e);
            }
        }
    }

    public void clearDanglingJobs() {
        if (configs.isCluster()) {
            crashCleaner.clearDanglingJobs();
        }
    }

    /**
     * Except seeder node, all others set seedDoneSignal (CountDownLatch) to
     * zero as they are all blocked by Barricade till seed is completed.
     */
    public void setSeedDoneSignal() {
        jobMediator.setSeedDoneSignal(0);
    }

    public void awaitForSeedDone() {
        try {
            jobMediator.awaitForSeedDone();
        } catch (InterruptedException e) {
            throw new CriticalException("await for seed done", e);
        }
    }

    public boolean isSeeder() {
        return seeder.get();
    }
}
