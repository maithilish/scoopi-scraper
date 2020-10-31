package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;

import com.google.common.util.concurrent.Uninterruptibles;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;
import com.hazelcast.transaction.TransactionalMap;

public class Barricade implements IBarricade {

    enum State {
        INITIALIZE, READY
    }

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ICluster cluster;
    @Inject
    private CrashedMembers crashedMembers;

    private TransactionOptions txOptions;
    private HazelcastInstance hz;
    private IMap<String, Object> objectMap;
    private String objectMapKey;

    private String key;
    private String ownerKey;

    // allow to cross barricade first and do something
    private boolean allowed = false;

    @Override
    public void setup(final String name) {
        key = name;

        ownerKey = key + "_owner";

        hz = (HazelcastInstance) cluster.getInstance();
        txOptions = new TransactionOptions()
                .setTransactionType(TransactionType.TWO_PHASE)
                .setTimeout(Integer.parseInt("10"), TimeUnit.SECONDS);
        objectMapKey = DsName.STORE_MAP.toString();
        objectMap = hz.getMap(DsName.STORE_MAP.toString());
    }

    @Override
    public void await() {

        String memberId = cluster.getMemberId();
        String memberIdShort = cluster.getShortId(memberId);
        final int retryDelay = 500;

        while (true) {
            TransactionContext tx1 = hz.newTransactionContext(txOptions);
            try {
                tx1.beginTransaction();
                TransactionalMap<String, Object> txCache =
                        tx1.getMap(objectMapKey);
                LOG.debug("{}, lock for update", key);
                State state = (State) txCache.getForUpdate(key);
                if (isNull(state)) {
                    txCache.set(key, State.INITIALIZE);
                    txCache.set(ownerKey, memberId);
                    allowed = true; // allow to cross barricade
                    LOG.debug("{}, node {} allowed to cross, state was {}", key,
                            memberIdShort, state);
                }
                tx1.commitTransaction();
                if (allowed) {
                    return;
                }
            } catch (Exception e) {
                allowed = false;
                tx1.rollbackTransaction();
                LOG.error("try to cross {}", key, e);
            }

            State state = (State) objectMap.get(key);
            if (nonNull(state) && state.equals(State.READY)) {
                return;
            } else {
                LOG.debug(
                        "{}, state {}, wait spinner, node {} not allowed to cross",
                        key, state, memberIdShort);
            }

            LOG.debug("crashed members {}",
                    Arrays.toString(crashedMembers.toArray()));

            String keyOwner = (String) objectMap.get(ownerKey);

            if (nonNull(keyOwner)) {
                String keyOwnerShort = cluster.getShortId(keyOwner);
                TransactionContext tx2 = hz.newTransactionContext(txOptions);
                if (crashedMembers.contains(keyOwner)) {
                    LOG.debug("{}, allowed node {} crashed, reset state", key,
                            keyOwnerShort);
                    try {
                        tx2.beginTransaction();
                        TransactionalMap<String, Object> txCache =
                                tx2.getMap(objectMapKey);
                        txCache.getForUpdate(key);
                        txCache.getForUpdate(ownerKey);
                        if (txCache.remove(ownerKey, keyOwner)) {
                            LOG.debug("{}, delete allowed node {}", key,
                                    keyOwnerShort);
                            if (txCache.remove(key, State.INITIALIZE)) {
                                LOG.debug("delete state {}", key);
                            } else {
                                LOG.debug("unable to delete state {}", key);
                            }
                        }
                        tx2.commitTransaction();
                    } catch (Exception e) {
                        tx2.rollbackTransaction();
                        LOG.error("{}, {}", key, e);
                    }
                } else {
                    LOG.debug(
                            "nothing to clean, barricade owner not in crashed list",
                            keyOwnerShort);
                }
            }
            Uninterruptibles.sleepUninterruptibly(retryDelay,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * If this method changes state to READY during extreme cluster topology
     * changes, other nodes may still see the stale state INITIALIZE. Call this
     * method from a while loop until isFinished() is true. See usage in
     * Bootstrap.setup().
     */
    @Override
    public void finish() {
        TransactionContext tx = hz.newTransactionContext(txOptions);
        try {
            tx.beginTransaction();
            TransactionalMap<String, Object> txCache = tx.getMap(objectMapKey);
            txCache.delete(ownerKey);
            txCache.set(key, State.READY);
            tx.commitTransaction();
            LOG.debug("{} finished, allow others", key);
        } catch (Exception e) {
            tx.rollbackTransaction();
            LOG.error("{}, {}", key, e);
        }
    }

    @Override
    public boolean isFinished() {
        TransactionContext tx = hz.newTransactionContext(txOptions);
        try {
            tx.beginTransaction();
            TransactionalMap<String, Object> txCache = tx.getMap(objectMapKey);
            State state = (State) txCache.getForUpdate(key);
            tx.commitTransaction();
            return nonNull(state) && state.equals(State.READY);
        } catch (Exception e) {
            tx.rollbackTransaction();
            LOG.error("{}, {}", key, e);
            return false;
        }
    }

    @Override
    public boolean isAllowed() {
        return allowed;
    }
}
