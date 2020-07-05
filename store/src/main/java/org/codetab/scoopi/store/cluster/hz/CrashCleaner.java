package org.codetab.scoopi.store.cluster.hz;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.store.ICluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalMap;

@Singleton
public class CrashCleaner {

    static final Logger LOG = LogManager.getLogger();

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
            LOG.debug("no crashed members");
            return false;
        }
        String leader = cluster.getLeader();
        if (!leader.equals(cluster.getMemberId())) {
            LOG.debug("not leader, don't reset crashed jobs. leader is {}",
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
                LOG.info("reset {} jobs taken by {}", takenJobs.size(),
                        crashedMemberId);
                TransactionContext tx = hz.newTransactionContext(txOptions);
                try {
                    tx.beginTransaction();
                    TransactionalMap<Long, ClusterJob> txJobsMap =
                            tx.getMap(DsName.JOBS_MAP.toString());
                    TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                            tx.getMap(DsName.TAKEN_JOBS_MAP.toString());

                    for (Long jobId : takenJobs) {
                        LOG.debug("reset taken job {}", jobId);
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
                    LOG.warn("could not reset jobs taken by {}, {}",
                            crashedMemberId, e.getLocalizedMessage());
                    LOG.debug("{}", e);
                }
            }
        }
        return true;
    }

    public void clearDanglingJobs() {
        Map<Long, ClusterJob> jobsMap = hz.getMap(DsName.JOBS_MAP.toString());
        jobsMap.clear();
    }
}
