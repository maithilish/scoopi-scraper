package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.ObjectUtils;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.Member;

@Singleton
public class Shutdown implements IShutdown {

    static final Logger LOGGER = LoggerFactory.getLogger(Shutdown.class);

    @Inject
    private ICluster cluster;
    @Inject
    private IJobStore jobStore;

    private Map<String, Boolean> doneMap;
    private Map<String, Boolean> terminateMap;

    private HazelcastInstance hz;

    private String memberId;

    private Cluster clst;

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
            LOGGER.warn("set terminate {}", e.getLocalizedMessage());
        }
    }

    @Override
    public <T> boolean tryShutdown(final Function<T, Boolean> func, final T t) {
        try {
            if (clst.getMembers().stream().map(Member::getUuid).map(uuid -> {
                return ObjectUtils.defaultIfNull(doneMap.get(uuid), false);
            }).anyMatch(v -> v.equals(false))) {
                LOGGER.debug("try shutdown, all not done, job store isDone {}",
                        jobStore.isDone());
                return false;
            }
        } catch (NullPointerException e) {
            LOGGER.debug("try shutdown, {}", e);
            return false;
        }

        if (jobStore.isDone()) {
            return func.apply(t);
        } else {
            LOGGER.debug("try shutdown, all done, job store isDone false");
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
            LOGGER.info("try shutdown node");
            hz.shutdown();
            LOGGER.info("node shutdown completed");
        } catch (HazelcastInstanceNotActiveException e) {

        }
    }
}
