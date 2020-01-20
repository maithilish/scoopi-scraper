package org.codetab.scoopi.store.cluster.hz;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
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

    private Map<String, Boolean> crashMembersMap;
    private Map<Long, ClusterJob> takenJobsMap;
    private TransactionOptions txOptions;

    private HazelcastInstance hz;

    public void init() {
        hz = (HazelcastInstance) cluster.getInstance();
        txOptions = (TransactionOptions) cluster.getTxOptions(configs);
        crashMembersMap = hz.getMap(DsName.CRASHED_MEMBERS_MAP.toString());
        takenJobsMap = hz.getMap(DsName.TAKEN_JOBS_MAP.toString());
    }

    public void addCrashedMember(final String crashedMemberId) {
        crashMembersMap.putIfAbsent(crashedMemberId, false);
    }

    public boolean resetCrashedJobs() {
        if (!cluster.getLeader().equals(cluster.getMemberId())) {
            return false;
        }

        List<String> crashMembers = crashMembersMap.entrySet().stream()
                .filter(e -> e.getValue().equals(false)).map(e -> e.getKey())
                .collect(toList());

        for (String crashedMemberId : crashMembers) {
            List<Long> takenJobs = takenJobsMap.values().stream().filter(
                    p -> p.isTaken() && p.getMemberId().equals(crashedMemberId))
                    .map(ClusterJob::getJobId).collect(Collectors.toList());
            if (takenJobs.size() > 0) {
                TransactionContext tx = hz.newTransactionContext(txOptions);
                try {
                    tx.beginTransaction();
                    TransactionalMap<Long, ClusterJob> txJobsMap =
                            tx.getMap(DsName.JOBS_MAP.toString());
                    TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                            tx.getMap(DsName.TAKEN_JOBS_MAP.toString());
                    TransactionalMap<String, Boolean> txCrashMemberMap =
                            tx.getMap(DsName.CRASHED_MEMBERS_MAP.toString());

                    for (Long jobId : takenJobs) {
                        LOGGER.debug("reset taken job {}", jobId);
                        ClusterJob cJob = txTakenJobsMap.remove(jobId);
                        cJob.setTaken(false);
                        cJob.setMemberId(null);
                        txJobsMap.put(jobId, cJob);
                    }
                    txCrashMemberMap.set(crashedMemberId, true);
                    tx.commitTransaction();
                } catch (Exception e) {
                    tx.rollbackTransaction();
                    throw e;
                }
            }
        }
        return true;
    }
}
