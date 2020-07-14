package org.codetab.scoopi.engine.module;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.step.extract.JobSeeder;
import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.IJobStore;

public class JobSeedModule {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private JobSeeder jobSeeder;
    @Inject
    private IJobStore jobStore;
    @Inject
    private IBarricade jobSeedBrricade;

    public void seedJobs() {
        jobSeedBrricade.setup("jobSeedBarricade");

        // block all nodes except one
        jobSeedBrricade.await();

        if (jobSeedBrricade.isAllowed()) {
            // seeder node
            jobSeeder.clearDanglingJobs();
            jobSeeder.seedLocatorGroups();
            CompletableFuture.runAsync(() -> {
                jobSeeder.awaitForSeedDone();
                jobStore.setState(IJobStore.State.READY);
                jobSeedBrricade.finish();
            });
        } else {
            LOG.info("jobs are already seeded by another node");
        }
    }

    public void awaitForJobSeed() {
        jobSeeder.awaitForSeedDone();
    }
}
