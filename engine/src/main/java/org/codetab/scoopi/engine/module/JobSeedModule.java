package org.codetab.scoopi.engine.module;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.step.extract.JobSeeder;
import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.IJobStore;

import com.google.common.util.concurrent.Uninterruptibles;

public class JobSeedModule {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private JobSeeder jobSeeder;
    @Inject
    private IJobStore jobStore;
    @Inject
    private IBarricade jobSeedBrricade;

    public void seedJobs() {
        String barricadeName = "jobSeedBarricade";
        jobSeedBrricade.setup(barricadeName);

        // block all nodes except one
        jobSeedBrricade.await();

        if (jobSeedBrricade.isAllowed()) {
            // seeder node
            LOG.info("allowed to pass barricade, seed jobs");
            jobSeeder.clearDanglingJobs();
            jobSeeder.seedLocatorGroups();
            CompletableFuture.runAsync(() -> {
                LOG.debug("await for seed done");
                jobSeeder.awaitForSeedDone();

                final int retryDelay = 200;
                while (!jobSeedBrricade.isFinished()) {
                    LOG.info("try release {}", barricadeName);
                    jobSeedBrricade.finish();
                    Uninterruptibles.sleepUninterruptibly(retryDelay,
                            TimeUnit.MILLISECONDS);
                }
                LOG.debug("{} released", barricadeName);

                LOG.debug("set jobStore state to READY");
                jobStore.setState(IJobStore.State.READY);

                LOG.debug("job seed completed");
            });
        } else {
            LOG.info("jobs seeded by another node, cross {}", barricadeName);
        }
    }

    public void awaitForJobSeed() {
        jobSeeder.awaitForSeedDone();
    }
}
