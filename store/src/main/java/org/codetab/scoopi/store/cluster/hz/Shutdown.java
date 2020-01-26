package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.ClusterState;
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
        terminateMap.put(memberId, true);
    }

    @Override
    public <T> boolean tryShutdown(final Function<T, Boolean> func, final T t) {
        try {
            if (clst.getMembers().stream().map(Member::getUuid)
                    .map(uuid -> doneMap.get(uuid))
                    .anyMatch(v -> v.equals(false))) {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }

        if (jobStore.isDone()) {
            return func.apply(t);
        } else {
            return false;
        }
    }

    @Override
    public void tryTerminate() {
        try {
            LOGGER.info("try cluster shutdown");
            // get terminate status of active members
            if (clst.getMembers().stream().map(Member::getUuid)
                    .map(uuid -> terminateMap.get(uuid))
                    .anyMatch(v -> v.equals(false))) {
                LOGGER.info("failed, cluster has some active nodes");
                return;
            }

            LOGGER.info("all scoopi instances are completed, go for shutdown");
            if (clst.getClusterState().equals(ClusterState.ACTIVE)) {
                LOGGER.info("cluster shutdown initiated");
                clst.shutdown();
            } else {
                LOGGER.info("cluster shutdown already initiated");
            }

            LOGGER.info("cluster shutdown completed");
        } catch (HazelcastInstanceNotActiveException e) {

        }
    }
}
