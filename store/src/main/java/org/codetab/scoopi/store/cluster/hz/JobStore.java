package org.codetab.scoopi.store.cluster.hz;

import static org.codetab.scoopi.util.Util.spaceit;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.cluster.ICluster;
import org.codetab.scoopi.store.cluster.IClusterJobStore;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

@Singleton
public class JobStore implements IClusterJobStore {

    @Inject
    private Configs configs;
    @Inject
    private ICluster cluster;

    private HazelcastInstance hz;

    private Queue<Payload> jobQ;
    private Set<Payload> jobTakenSet;
    private Map<String, String> keyStoreMap;

    private String memberId;
    private int jobTakeLimit;
    private FlakeIdGenerator jobIdGenerator;

    @Override
    public boolean open() {
        try {
            hz = (HazelcastInstance) cluster.getInstance();
            jobQ = hz.getQueue("job");
            jobTakenSet = hz.getSet("takenJob");
            keyStoreMap = hz.getMap("keyStore");
            jobIdGenerator = hz.getFlakeIdGenerator("job_id_seq");
            memberId = configs.getConfig("scoopi.cluster.memberId");
            jobTakeLimit = Integer
                    .parseInt(configs.getConfig("scoopi.cluster.jobTakeLimit"));
        } catch (NumberFormatException | ConfigNotFoundException e) {
            throw new CriticalException(e);
        }
        return false;
    }

    @Override
    public boolean close() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createTables() {
        // TODO Auto-generated method stub
        return false;
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
        System.out.println("remove job " + id);
        Optional<Payload> payload = jobTakenSet.stream()
                .filter(p -> p.getJobInfo().getId() == id).findFirst();
        if (payload.isPresent()) {
            // System.out.println("remove job " + payload.get().getJobInfo());
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
        return State.valueOf(keyStoreMap.get("data_grid_state"));
    }

    @Override
    public void setState(final State state) {
        keyStoreMap.put("data_grid_state", state.toString());
    }

    // FIXME make it atomic
    @Override
    public boolean changeStateToInitialize() {
        String key = "data_grid_state";
        if (keyStoreMap.containsKey(key)) {
            if (keyStoreMap.get(key).equals(State.NEW.toString())) {
                keyStoreMap.put(key, State.INITIALIZE.toString());
                return true;
            } else {
                return false;
            }
        } else {
            keyStoreMap.put(key, State.INITIALIZE.toString());
            return true;
        }
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
        long l = jobIdGenerator.newId();
        System.out.println("sq " + l);
        return l;
    }
}
