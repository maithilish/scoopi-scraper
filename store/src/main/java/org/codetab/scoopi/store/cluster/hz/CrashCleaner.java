package org.codetab.scoopi.store.cluster.hz;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.model.ClusterJob;
import org.codetab.scoopi.store.ICluster;

import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalList;
import com.hazelcast.transaction.TransactionalMap;

@Singleton
public class CrashCleaner {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private CrashedMembers crashedMembers;

    /*
     * set of crashed members that are processed by CrashCleaner
     */
    private Set<String> clearedMembers;

    private Map<Long, ClusterJob> takenJobsMap;
    private TransactionOptions txOptions;

    private HazelcastInstance hz;

    public void init() {
        hz = (HazelcastInstance) cluster.getInstance();
        txOptions = (TransactionOptions) cluster.getTxOptions(configs);
        takenJobsMap = hz.getMap(DsName.TAKEN_JOBS_MAP.toString());
        clearedMembers = new HashSet<String>();
    }

    public boolean resetCrashedJobs() {

        if (crashedMembers.isEmpty()) {
            return false;
        }

        String leader = cluster.getLeader();
        if (!cluster.getMemberId().equals(leader)) {
            return false; // not leader
        }

        /*
         * get set of members that are in crashedMembers but not yet cleared by
         * cleaner and find find first in it.
         */
        Optional<String> crashedMember =
                crashedMembers.difference(clearedMembers).stream().findFirst();
        if (crashedMember.isPresent()) {
            LOG.debug("crashed members {}",
                    Arrays.toString(crashedMembers.toArray()));
            LOG.debug("i am {} leader, reset taken jobs",
                    cluster.getShortId(leader));
        } else {
            return false;
        }

        String crashedMemberId = crashedMember.get();

        List<Long> takenJobs = takenJobsMap.values().stream().filter(
                p -> p.isTaken() && p.getMemberId().equals(crashedMemberId))
                .map(ClusterJob::getJobId).collect(Collectors.toList());

        if (takenJobs.isEmpty()) {
            // no taken job by crashed node, add it to cleared set
            clearedMembers.add(crashedMemberId);
            LOG.debug("no taken jobs by {}, crashed member removed",
                    cluster.getShortId(crashedMemberId));
            return false;
        } else {
            LOG.info("reset {} jobs taken by {}", takenJobs.size(),
                    cluster.getShortId(crashedMemberId));
            TransactionContext tx = hz.newTransactionContext(txOptions);
            try {
                tx.beginTransaction();
                TransactionalList<ClusterJob> txJobsList =
                        tx.getList(DsName.JOBS_LIST.toString());
                TransactionalMap<Long, ClusterJob> txTakenJobsMap =
                        tx.getMap(DsName.TAKEN_JOBS_MAP.toString());

                for (Long jobId : takenJobs) {
                    LOG.debug("reset taken job {}", jobId);
                    ClusterJob cJob = txTakenJobsMap.remove(jobId);
                    cJob.setTaken(false);
                    cJob.setMemberId(null);
                    txJobsList.add(cJob);
                }
                tx.commitTransaction();
                LOG.debug(
                        "reset taken jobs finished, crashed node will be removed in next cycle");
                return true;
            } catch (Exception e) {
                tx.rollbackTransaction();
                LOG.warn("could not reset jobs taken by {}, {}",
                        cluster.getShortId(crashedMemberId),
                        e.getLocalizedMessage());
                LOG.debug("{}", e);
                return false;
            }
        }
    }

    public void clearDanglingJobs() {
        IList<ClusterJob> jobsMap = hz.getList(DsName.JOBS_LIST.toString());
        jobsMap.clear();
    }

    public boolean hasCrashedMembers() {
        return !crashedMembers.isEmpty();
    }
}
