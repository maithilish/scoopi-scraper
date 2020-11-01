package org.codetab.scoopi.step.extract;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.base.PayloadFactory;
import org.codetab.scoopi.step.mediator.TaskMediator;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;

@Singleton
public class JobSeeder {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private ILocatorDef locatorDef;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private PayloadFactory payloadFactory;
    @Inject
    private Errors errors;
    @Inject
    private CrashCleaner crashCleaner;

    /**
     * Except seeder node, all others set seedDoneSignal (CountDownLatch) to
     * zero as they are all blocked by Barricade till seed is completed.
     */
    private CountDownLatch seedLatch = new CountDownLatch(0);

    public void seedLocatorGroups() {
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

        // only by seeder node
        seedLatch = new CountDownLatch(locatorGroups.size());
        LOG.debug("job seed countdown latch set: {}", locatorGroups.size());

        final List<Payload> payloads = payloadFactory
                .createSeedPayloads(locatorGroups, stepName, seederClzName);
        for (final Payload payload : payloads) {
            try {
                taskMediator.pushPayload(payload);
            } catch (InterruptedException e) {
                String group = payload.getJobInfo().getGroup();
                errors.inc();
                LOG.error("seed locator group: {} [{}]", group, ERROR.INTERNAL,
                        e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void clearDanglingJobs() {
        if (configs.isCluster()) {
            crashCleaner.clearDanglingJobs();
        }
    }

    public void awaitForSeedDone() {
        try {
            LOG.debug("await for job seed completion");
            seedLatch.await();
            LOG.debug("job seed completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CriticalException("await for job seed completion", e);
        }
    }

    public void countDownSeedLatch() {
        seedLatch.countDown();
        LOG.debug("count down job seed latch by [1]");
    }

}
