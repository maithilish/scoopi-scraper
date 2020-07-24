package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

@Singleton
public class ClusterShutdown implements IShutdown {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ICluster cluster;
    @Inject
    private IJobStore jobStore;

    private Map<String, Boolean> doneMap;
    private Map<String, Boolean> terminateMap;

    private HazelcastInstance hz;

    private String memberId;

    private com.hazelcast.cluster.Cluster clst;

    private AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public void init() {
        hz = (HazelcastInstance) cluster.getInstance();
        clst = hz.getCluster();
        memberId = cluster.getMemberId();
        doneMap = hz.getMap(DsName.MEMBER_DONE_MAP.toString());
        terminateMap = hz.getMap(DsName.MEMBER_TERMINATE_MAP.toString());
        doneMap.put(memberId, false);
        terminateMap.put(memberId, false);
    }

    @Override
    public void setDone() {
        if (cluster.isNodeRunning()) {
            doneMap.put(memberId, true);
        }
    }

    @Override
    public void setTerminate() {
        if (cluster.isNodeRunning()) {
            try {
                terminateMap.put(memberId, true);
            } catch (HazelcastInstanceNotActiveException e) {
                LOG.warn("set terminate {}", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void terminate() {
        // prefer node shutdown as cluster shutdown occasionally fails
        try {
            LOG.info("try node shutdown");
            hz.shutdown();
            LOG.info("node shutdown completed");
        } catch (HazelcastInstanceNotActiveException e) {
            LOG.debug("try terminate", e);
        }
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public boolean allNodesDone() {
        boolean allDone = true;
        try {
            if (clst.getMembers().stream().map(Member::getUuid).map(uuid -> {
                return ObjectUtils.defaultIfNull(doneMap.get(uuid.toString()),
                        false);
            }).anyMatch(v -> v.equals(false))) {
                allDone = false;
            }
            return allDone;
        } catch (NullPointerException e) {
            LOG.debug("is all node done", e);
            return false;
        }
    }

    @Override
    public boolean jobStoreDone() {
        return jobStore.isDone();
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }
}
