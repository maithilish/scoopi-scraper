package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;
import java.util.function.Function;

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
public class Shutdown implements IShutdown {

    static final Logger LOG = LogManager.getLogger();

    @Inject
    private ICluster cluster;
    @Inject
    private IJobStore jobStore;

    private Map<String, Boolean> doneMap;
    private Map<String, Boolean> terminateMap;

    private HazelcastInstance hz;

    private String memberId;

    private com.hazelcast.cluster.Cluster clst;

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
        doneMap.put(memberId, true);
    }

    @Override
    public void setTerminate() {
        // FIXME - null or hz not active exception
        try {
            terminateMap.put(memberId, true);
        } catch (HazelcastInstanceNotActiveException e) {
            LOG.warn("set terminate {}", e.getLocalizedMessage());
        }
    }

    @Override
    public <T> boolean tryShutdown(final Function<T, Boolean> func, final T t) {
        try {
            if (clst.getMembers().stream().map(Member::getUuid).map(uuid -> {
                return ObjectUtils.defaultIfNull(doneMap.get(uuid.toString()),
                        false);
            }).anyMatch(v -> v.equals(false))) {
                LOG.debug("try shutdown, all not done, job store isDone {}",
                        jobStore.isDone());
                return false;
            }
        } catch (NullPointerException e) {
            LOG.debug("try shutdown, {}", e);
            return false;
        }

        if (jobStore.isDone()) {
            return func.apply(t);
        } else {
            LOG.debug("try shutdown, all done, job store isDone false");
            return false;
        }
    }

    @Override
    public void tryTerminate() {
        /**
         * shutdown initiated at cluster level fails sometimes, so reverted back
         * to node shutdown.
         */
        try {
            LOG.info("try shutdown node");
            hz.shutdown();
            LOG.info("node shutdown completed");
        } catch (HazelcastInstanceNotActiveException e) {

        }
    }
}
