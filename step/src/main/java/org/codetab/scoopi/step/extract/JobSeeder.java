package org.codetab.scoopi.step.extract;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.ILocatorDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.TransactionException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.step.JobMediator;
import org.codetab.scoopi.step.PayloadFactory;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IJobStore.State;
import org.codetab.scoopi.store.cluster.hz.CrashCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobSeeder {

    static final Logger LOGGER = LoggerFactory.getLogger(JobSeeder.class);

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
    private ErrorLogger errorLogger;
    @Inject
    private IJobStore jobStore;
    @Inject
    private CrashCleaner crashCleaner;

    private AtomicBoolean seedPermit = new AtomicBoolean(false);

    public boolean acquirePermitToSeed() {
        try {
            if (jobStore.changeStateToInitialize()) {
                seedPermit.set(true);
                // global time
                jobStore.setRunDateTime(configs.getRunDateTimeString());
                LOGGER.info("acquired lock to seed jobs");
                return true;
            } else {
                seedPermit.set(false);
            }
        } catch (TransactionException e) {
            seedPermit.set(false);
        }

        LOGGER.info("wait for jobs seed to finish...");
        while (true) {
            // FIXME - enum is null, change sleep to wait-notify
            if (jobStore.getState().equals(State.READY)) {
                seedPermit.set(false);
                LOGGER.info("jobs seed finished");
                jobMediator.setSeedDoneSignal(0);
                break;
            }
            crashCleaner.resetSeedState();
            try {
                if (jobStore.changeStateToInitialize()) {
                    seedPermit.set(true);
                    LOGGER.info("acquired lock to seed jobs");
                    break;
                }
            } catch (TransactionException e) {
            }
            try {
                LOGGER.debug("jobStore state {}, wait for Ready",
                        jobStore.getState());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        return seedPermit.get();
    }

    public boolean seedLocatorGroups() {
        if (seedPermit.get()) {
            LOGGER.info("seed defined locator groups");
            final String stepName = "start"; //$NON-NLS-1$
            String seederClzName = null;
            try {
                seederClzName = configs.getConfig("scoopi.seeder.class"); //$NON-NLS-1$
            } catch (final ConfigNotFoundException e) {
                final String message = "unable seed locator group";
                throw new CriticalException(message, e);
            }

            final List<LocatorGroup> locatorGroups =
                    locatorDef.getLocatorGroups();
            jobMediator.setSeedDoneSignal(locatorGroups.size()); // CountDownLatch

            final List<Payload> payloads = payloadFactory
                    .createSeedPayloads(locatorGroups, stepName, seederClzName);
            for (final Payload payload : payloads) {
                try {
                    taskMediator.pushPayload(payload);
                } catch (final InterruptedException e) {
                    final String group = payload.getJobInfo().getGroup();
                    final String message =
                            spaceit("seed locator group: ", group);
                    errorLogger.log(CAT.INTERNAL, message, e);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isSeeder() {
        return seedPermit.get();
    }

    public void clearDanglingJobs() {
        if (configs.isCluster()) {
            crashCleaner.clearDanglingJobs();
        }
    }
}
