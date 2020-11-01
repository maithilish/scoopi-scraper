package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.isNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.store.cluster.hz.Barricade.State;

import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;

public class BarricadeTx {

    private static final Logger LOG = LogManager.getLogger();

    public boolean acquire(final String barricadeKey, final String memberId,
            final TransactionContext tx) {

        boolean allowed = false;
        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();

        try {
            tx.beginTransaction();
            TransactionalMap<String, Object> txCache = tx.getMap(objectMapKey);
            LOG.debug("{}, lock for update", barricadeKey);
            State state = (State) txCache.getForUpdate(barricadeKey);
            if (isNull(state)) {
                txCache.set(barricadeKey, State.INITIALIZE);
                txCache.set(ownerKey, memberId);
                allowed = true; // allow to cross barricade
            }
            tx.commitTransaction();
            LOG.debug("tx commited");
        } catch (Exception e) {
            allowed = false;
            LOG.error("try to cross {}", barricadeKey, e);
            tx.rollbackTransaction();
            LOG.debug("tx rolledback");
        }
        return allowed;
    }

    public void reset(final String barricadeKey, final String owner,
            final String ownerShort, final TransactionContext tx2) {

        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();

        try {
            tx2.beginTransaction();
            TransactionalMap<String, Object> txCache = tx2.getMap(objectMapKey);
            txCache.getForUpdate(barricadeKey);
            txCache.getForUpdate(ownerKey);
            if (txCache.remove(ownerKey, owner)) {
                LOG.debug("{}, delete allowed node {}", barricadeKey,
                        ownerShort);
                if (txCache.remove(barricadeKey, State.INITIALIZE)) {
                    LOG.debug("delete state {}", barricadeKey);
                } else {
                    LOG.debug("unable to delete state {}", barricadeKey);
                }
            }
            tx2.commitTransaction();
        } catch (Exception e) {
            tx2.rollbackTransaction();
            LOG.error("{}, {}", barricadeKey, e);
        }
    }

    public boolean release(final String barricadeKey,
            final TransactionContext tx) {

        String ownerKey = barricadeKey + "_owner";
        String objectMapKey = DsName.STORE_MAP.toString();
        try {
            tx.beginTransaction();
            TransactionalMap<String, Object> txCache = tx.getMap(objectMapKey);
            txCache.delete(ownerKey);
            txCache.set(barricadeKey, State.READY);
            tx.commitTransaction();
            LOG.debug("{} finished, allow others", barricadeKey);
            return true;
        } catch (Exception e) {
            tx.rollbackTransaction();
            LOG.error("{}, {}", barricadeKey, e);
            return false;
        }
    }
}
