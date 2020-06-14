package org.codetab.scoopi.store.cluster.hz;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.store.ICluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;

@Singleton
public class CrashCleaner {

    static final Logger LOGGER = LoggerFactory.getLogger(CrashCleaner.class);

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private MembershipListener membershipListener;

    private Map<Long, ClusterJob> takenJobsMap;
    private TransactionOptions txOptions;

    private HazelcastInstance hz;

    public void init() {
        hz = (HazelcastInstance) cluster.getInstance();
        txOptions = (TransactionOptions) cluster.getTxOptions(configs);
        takenJobsMap = hz.getMap(DsName.TAKEN_JOBS_MAP.toString());
    }

    public boolean resetCrashedJobs() {
        Stack<String> crashedMembers = membershipListener.getCrashedMembers();
        if (crashedMembers.isEmpty()) {
            LOGGER.debug("no crashed members");
            return false;
        }
        String leader = cluster.getLeader();
        if (!leader.equals(cluster.getMemberId())) {
            LOGGER.debug("not leader, don't reset crashed jobs. leader is {}",
                    leader);
            return false;
        }

        while (!crashedMembers.isEmpty()) {
            String crashedMemberId = crashedMembers.peek();

            List<Long> takenJobs = takenJobsMap.values().stream().filter(
                    p -> p.isTaken() && p.getMemberId().equals(crashedMemberId))
                    .map(ClusterJob::getJobId).collect(Collectors.toList());

            if (takenJobs.isEmpty()) {
                // no taken job by crashed node, remove it
                crashedMembers.pop();
            } else {
                LOGGER.info("reset {} jobs taken by {}", takenJobs.size(),
                        crashedMemberId);
                TransactionContext tx = hz.newTransactionContext(txOptions);
                try {
                    tx.beginTransaction();
                    TransactionalMap<Long, ClusterJob> txJobsMap =
                            tx.getMap(DsName.JOBS_MAP.toString());
                    TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                            tx.getMap(DsName.TAKEN_JOBS_MAP.toString());

                    for (Long jobId : takenJobs) {
                        LOGGER.debug("reset taken job {}", jobId);
                        ClusterJob cJob = txTakenJobsMap.remove(jobId);
                        cJob.setTaken(false);
                        cJob.setMemberId(null);
                        txJobsMap.put(jobId, cJob);
                    }
                    tx.commitTransaction();
                    // done, remove the crashed node
                    crashedMembers.pop();
                } catch (Exception e) {
                    tx.rollbackTransaction();
                    LOGGER.warn("could not reset jobs taken by {}, {}",
                            crashedMemberId, e.getLocalizedMessage());
                    LOGGER.debug("{}", e);
                }
            }
        }
        return true;
    }

    public void clearDanglingJobs() {
        Map<Long, ClusterJob> jobsMap = hz.getMap(DsName.JOBS_MAP.toString());
        jobsMap.clear();
    }

    // FIXME - bootfix, remove this
    // public void resetSeedState() {
    // Stack<String> crashedMembers = membershipListener.getCrashedMembers();
    // if (crashedMembers.isEmpty()) {
    // return;
    // }
    //
    // TransactionContext tx = hz.newTransactionContext(txOptions);
    // try {
    // tx.beginTransaction();
    // TransactionalMap<String, String> txKeyStoreMap =
    // tx.getMap(DsName.KEYSTORE_MAP.toString());
    //
    // if (txKeyStoreMap.getForUpdate(DsName.DATA_GRID_STATE.toString())
    // .equalsIgnoreCase(State.INITIALIZE.toString())) {
    // String seederMemberId =
    // txKeyStoreMap.getForUpdate(DsName.SEEDER_ID.toString());
    // if (nonNull(seederMemberId)
    // && crashedMembers.contains(seederMemberId)) {
    // LOGGER.info(
    // "job seeder is crashed, reset job store state to NEW");
    // txKeyStoreMap.set(DsName.DATA_GRID_STATE.toString(),
    // State.NEW.toString());
    // if (!txKeyStoreMap.remove(DsName.SEEDER_ID.toString(),
    // seederMemberId)) {
    // throw new IllegalStateException(
    // "another node has already reset the seed state");
    // }
    // }
    // }
    // tx.commitTransaction();
    // } catch (Exception e) {
    // tx.rollbackTransaction();
    // LOGGER.warn("could not reset seed state, {}",
    // e.getLocalizedMessage());
    // LOGGER.debug("{}", e);
    // }
    //
    // }

}
