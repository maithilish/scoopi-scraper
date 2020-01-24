package org.codetab.scoopi.store.cluster.hz;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.store.ICluster;
import org.codetab.scoopi.store.IJobStore;
import org.codetab.scoopi.store.IShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.transaction.TransactionOptions;

@Singleton
public class Shutdown implements IShutdown {

    static final Logger LOGGER = LoggerFactory.getLogger(CrashCleaner.class);

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private IJobStore jobStore;

    private Map<String, Boolean> doneMap;
    private Map<String, Boolean> terminateMap;

    private TransactionOptions txOptions;

    private HazelcastInstance hz;

    AtomicInteger doneCount = new AtomicInteger();
    AtomicInteger tryCount = new AtomicInteger();
    AtomicInteger tryDoneCount = new AtomicInteger();
    AtomicInteger shutdownCount = new AtomicInteger();

    private String memberId;

    @Override
    public void init() {
        hz = (HazelcastInstance) cluster.getInstance();
        memberId = cluster.getMemberId();
        txOptions = (TransactionOptions) cluster.getTxOptions(configs);
        doneMap = hz.getMap(DsName.MEMBER_DONE_MAP.toString());
        terminateMap = hz.getMap(DsName.MEMBER_TERMINATE_MAP.toString());
        doneMap.put(memberId, false);
        terminateMap.put(memberId, false);
    }

    @Override
    public void setDone() {
        doneMap.put(memberId, true);
        doneCount.getAndIncrement();
    }

    @Override
    public void setTerminate() {
        terminateMap.put(memberId, true);
    }

    @Override
    public <T, R> void tryShutdown(final Function<T, R> func, final T t) {
        if (doneMap.values().stream().anyMatch(v -> v.equals(false))) {
            tryCount.getAndIncrement();
            return;
        }
        tryDoneCount.getAndIncrement();
        System.out.printf("%d %d %d %d\n", doneCount.get(), tryCount.get(),
                tryDoneCount.get(), shutdownCount.get());
        if (jobStore.isDone()) {
            func.apply(t);
            shutdownCount.getAndIncrement();
            System.out.printf("%d %d %d %d\n", doneCount.get(), tryCount.get(),
                    tryDoneCount.get(), shutdownCount.get());
        }
    }

    @Override
    public void tryTerminate() {
        try {
            if (terminateMap.values().stream().anyMatch(v -> v.equals(false))) {
                return;
            }
            HazelcastInstance hz = (HazelcastInstance) cluster.getInstance();
            Cluster clst = hz.getCluster();

            if (clst.getClusterState().equals(ClusterState.ACTIVE)) {
                clst.shutdown();
            }
        } catch (HazelcastInstanceNotActiveException e) {

        }
    }
}
