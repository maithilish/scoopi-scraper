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

                LOG.debug("set jobStore state to READY");
                jobStore.setState(IJobStore.State.READY);

                jobSeedBrricade.finish();
                LOG.debug("job seed done");
            });
        } else {
            LOG.info("jobs seeded by another node, cross {}", barricadeName);
        }
    }

    public void awaitForJobSeed() {
        jobSeeder.awaitForSeedDone();
    }
}
