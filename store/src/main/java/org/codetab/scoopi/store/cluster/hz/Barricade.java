package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.store.IBarricade;
import org.codetab.scoopi.store.ICluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionOptions.TransactionType;

public class Barricade implements IBarricade {

    enum State {
        INITIALIZE, READY
    }

    static final Logger LOGGER = LoggerFactory.getLogger(Barricade.class);

    @Inject
    private ICluster cluster;
    @Inject
    private MembershipListener membershipListener;

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
        int retryDelay = Integer.parseInt("500");

        while (true) {
            TransactionContext tx1 = hz.newTransactionContext(txOptions);
            try {
                tx1.beginTransaction();
                TransactionalMap<String, Object> txCache =
                        tx1.getMap(objectMapKey);
                LOGGER.debug("lock key {} for update", key);
                State state = (State) txCache.getForUpdate(key);
                if (isNull(state)) {
                    txCache.set(key, State.INITIALIZE);
                    txCache.set(ownerKey, memberId);
                    allowed = true; // allow to cross barricade
                    LOGGER.debug("state is {}, node {} allowed to cross {}",
                            state, memberId, key);
                }
                tx1.commitTransaction();
                if (allowed) {
                    return;
                }
            } catch (Exception e) {
                allowed = false;
                tx1.rollbackTransaction();
                LOGGER.error("{}", e);
            }

            LOGGER.debug("wait on {}", key);
            State state = (State) objectMap.get(key);
            if (nonNull(state) && state.equals(State.READY)) {
                return;
            }

            Stack<String> crashedMembers =
                    membershipListener.getCrashedMembers();
            String keyOwner = (String) objectMap.get(ownerKey);

            TransactionContext tx2 = hz.newTransactionContext(txOptions);
            if (crashedMembers.contains(keyOwner)) {
                LOGGER.debug("allowed node crashed, reset state {}");
                try {
                    tx2.beginTransaction();
                    TransactionalMap<String, Object> txCache =
                            tx2.getMap(objectMapKey);
                    txCache.getForUpdate(key);
                    txCache.getForUpdate(ownerKey);
                    if (txCache.remove(ownerKey, keyOwner)) {
                        LOGGER.debug("remove allowed node {}", keyOwner);
                        if (txCache.remove(key, State.INITIALIZE)) {
                            LOGGER.debug("remove state {}", key);
                        } else {
                            LOGGER.debug("unable to remove state {}", key);
                        }
                    }
                    tx2.commitTransaction();
                } catch (Exception e) {
                    tx2.rollbackTransaction();
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(retryDelay);
            } catch (InterruptedException e) {
                throw new CriticalException("barricade interrupted");
            }
        }
    }

    @Override
    public void finish() {
        TransactionContext tx = hz.newTransactionContext(txOptions);
        try {
            tx.beginTransaction();
            TransactionalMap<String, Object> txCache = tx.getMap(objectMapKey);
            txCache.delete(ownerKey);
            txCache.set(key, State.READY);
            tx.commitTransaction();
            LOGGER.debug("barricade {} removed", key);
        } catch (Exception e) {
            tx.rollbackTransaction();
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAllowed() {
        return allowed;
    }
}
