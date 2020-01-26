package org.codetab.scoopi.store.cluster.hz;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            return false;
        }
        if (!cluster.getLeader().equals(cluster.getMemberId())) {
            return false;
        }

        Set<String> failedItems = new HashSet<>();

        while (!crashedMembers.isEmpty()) {

            String crashedMemberId = crashedMembers.pop();

            List<Long> takenJobs = takenJobsMap.values().stream().filter(
                    p -> p.isTaken() && p.getMemberId().equals(crashedMemberId))
                    .map(ClusterJob::getJobId).collect(Collectors.toList());

            if (takenJobs.size() > 0) {
                LOGGER.info("reset taken jobs {} by {}", takenJobs.size(),
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
                } catch (Exception e) {
                    failedItems.add(crashedMemberId);
                    tx.rollbackTransaction();
                    throw e;
                }
            }
        }
        // add back failed members
        crashedMembers.addAll(failedItems);
        return true;
    }
}
