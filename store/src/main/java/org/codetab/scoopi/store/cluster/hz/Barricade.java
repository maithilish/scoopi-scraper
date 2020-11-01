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

public class Barricade implements IBarricade {

    public enum State {
        INITIALIZE, READY
    }

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ICluster cluster;
    @Inject
    private CrashedMembers crashedMembers;
    @Inject
    private BarricadeTx barricadeTx;

    private TransactionOptions txOptions;
    private HazelcastInstance hz;
    private IMap<String, Object> objectMap;

    private String barricadeKey;
    private String ownerKey;

    private boolean allowedToCross = false;

    @Override
    public void setup(final String name) {
        barricadeKey = name;
        ownerKey = barricadeKey + "_owner";

        hz = (HazelcastInstance) cluster.getInstance();
        txOptions = new TransactionOptions()
                .setTransactionType(TransactionType.TWO_PHASE)
                .setTimeout(Integer.parseInt("10"), TimeUnit.SECONDS);
        objectMap = hz.getMap(DsName.STORE_MAP.toString());
    }

    @Override
    public void await() {

        String memberId = cluster.getMemberId();
        String memberIdShort = cluster.getShortId(memberId);
        final int retryDelay = 500;

        while (true) {

            State state = (State) objectMap.get(barricadeKey);
            if (isNull(state)) {
                allowedToCross = barricadeTx.acquire(barricadeKey, memberId,
                        hz.newTransactionContext(txOptions));
                if (allowedToCross) {
                    LOG.debug("{}, node {} allowed to cross", barricadeKey,
                            memberIdShort);
                    return;
                }
            }

            state = (State) objectMap.get(barricadeKey);
            if (nonNull(state) && state.equals(State.READY)) {
                return;
            } else {
                LOG.debug(
                        "{}, state {}, wait spinner, node {} not allowed to cross",
                        barricadeKey, state, memberIdShort);
            }

            LOG.debug("crashed members {}",
                    Arrays.toString(crashedMembers.toArray()));

            String owner = (String) objectMap.get(ownerKey);

            if (nonNull(owner)) {
                String ownerShort = cluster.getShortId(owner);
                TransactionContext tx2 = hz.newTransactionContext(txOptions);
                if (crashedMembers.contains(owner)) {
                    LOG.debug("{}, allowed node {} crashed, reset state",
                            barricadeKey, ownerShort);
                    barricadeTx.reset(barricadeKey, owner, ownerShort, tx2);
                } else {
                    LOG.debug(
                            "nothing to clean, barricade owner not in crashed list",
                            ownerShort);
                }
            }
            Uninterruptibles.sleepUninterruptibly(retryDelay,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * If this method updates state to READY during extreme cluster topology
     * changes, other nodes may still see the stale state INITIALIZE. Call this
     * from a while loop until isFinished() is true.
     * <p>
     * See usage in Bootstrap.setup().
     */
    @Override
    public void release() {
        TransactionContext tx = hz.newTransactionContext(txOptions);
        barricadeTx.release(barricadeKey, tx);
    }

    @Override
    public boolean isReleased() {
        State state = (State) objectMap.get(barricadeKey);
        return nonNull(state) && state.equals(State.READY);
    }

    @Override
    public boolean isAllowed() {
        return allowedToCross;
    }
}
