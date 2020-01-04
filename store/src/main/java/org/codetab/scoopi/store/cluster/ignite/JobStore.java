package org.codetab.scoopi.store.cluster.ignite;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.cluster.ICluster;
import org.codetab.scoopi.store.cluster.IClusterJobStore;

@Singleton
public class JobStore implements IClusterJobStore {


    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;
    @Inject
    private CollectionConfiguration collectionCfg;

    private Ignite ignite;

    private Queue<Payload> jobQ;
    private Set<Payload> jobTakenSet;
    private Set<Entry<String, String>> keyStoreSet;
    private IgniteAtomicLong jobIdSeq;

    private String memberId;
    private int jobTakeLimit;

    @Override
    public boolean open() {
        try {
            ignite = (Ignite) cluster.getInstance();
            memberId = configs.getConfig("scoopi.cluster.memberId");
            jobTakeLimit = Integer
                    .parseInt(configs.getConfig("scoopi.cluster.jobTakeLimit"));
            jobIdSeq = ignite.atomicLong("job_id_seq", 0, true);

            // configure atomic cache
            collectionCfg.setCollocated(true);
            collectionCfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
            collectionCfg.setBackups(1);
            collectionCfg.setCacheMode(CacheMode.REPLICATED);

            // create distributed collections
            jobQ = ignite.queue("job", 0, collectionCfg);
            jobTakenSet = ignite.set("takenJob", collectionCfg);
            keyStoreSet = ignite.set("keyStore", collectionCfg);
            return true;
        } catch (NumberFormatException | ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
    }

    @Override
    public boolean close() {
        // ScoopiEngine stops cluster
        return true;
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        jobQ.offer(payload);
        return true;
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        Payload payload = jobQ.poll();
        if (Objects.isNull(payload)) {
            throw new NoSuchElementException("jobs queue is empty");
        } else {
            jobTakenSet.add(payload);
            return payload;
        }
    }

    @Override
    public boolean markFinished(final long id) {
        Optional<Payload> payload = jobTakenSet.stream()
                .filter(p -> p.getJobInfo().getId() == id).findFirst();
        if (payload.isPresent()) {
            return jobTakenSet.remove(payload.get());
        } else {
            throw new IllegalStateException(spaceit(
                    "mark finish, no such job, id:", String.valueOf(id)));
        }
    }

    @Override
    public int getJobCount() {
        return jobQ.size();
    }

    @Override
    public boolean isDone() {
        return jobQ.size() == 0;
    }

    @Override
    public State getState() {
        String key = "data_grid_state";
        Optional<Entry<String, String>> entry = getEntry(key);
        if (entry.isPresent()) {
            return State.valueOf(entry.get().getValue());
        } else {
            return State.NEW;
        }
    }

    @Override
    public void setState(final State state) {
        String key = "data_grid_state";
        Entry<String, String> entry =
                new SimpleImmutableEntry<String, String>(key, state.toString());

        Optional<Entry<String, String>> oldEntry = getEntry(key);
        if (oldEntry.isPresent()) {
            keyStoreSet.remove(oldEntry.get());
            keyStoreSet.add(entry);
        } else {
            keyStoreSet.add(entry);
        }
    }

    // FIXME make it atomic
    /**
     * transit state from NEW or no entry to ININITIALIZE
     */
    @Override
    public boolean changeStateToInitialize() {
        String key = "data_grid_state";
        Entry<String, String> initialize =
                new SimpleImmutableEntry<String, String>(key,
                        State.INITIALIZE.toString());

        Optional<Entry<String, String>> oldEntry = getEntry(key);
        if (oldEntry.isPresent()) {
            if (oldEntry.get().getValue().equals(State.NEW.toString())) {
                // change new to initialize
                keyStoreSet.remove(oldEntry.get());
                keyStoreSet.add(initialize);
                return true;
            } else {
                return false;
            }
        } else {
            // no entry, set to initialize
            keyStoreSet.add(initialize);
            return true;
        }
    }

    private Optional<Entry<String, String>> getEntry(final String key) {
        return keyStoreSet.stream().filter(e -> e.getKey().equals(key))
                .findFirst();
    }

    @Override
    public String getNodeId() {
        return memberId;
    }

    @Override
    public int getJobTakenCount() {
        return jobTakenSet.size();
    }

    @Override
    public int getJobTakeLimit() {
        return jobTakeLimit;
    }

    @Override
    public long getJobIdSeq() {
        return jobIdSeq.getAndIncrement();
    }
}
