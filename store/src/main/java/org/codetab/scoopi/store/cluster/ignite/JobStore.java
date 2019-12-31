package org.codetab.scoopi.store.cluster.ignite;

import static org.codetab.scoopi.util.Util.spaceit;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.cluster.IClusterJobStore;
import org.codetab.scoopi.store.cluster.ignite.dao.CacheDao;
import org.codetab.scoopi.store.cluster.ignite.dao.JobDao;
import org.codetab.scoopi.store.cluster.ignite.dao.KeyStoreDao;

@Singleton
public class JobStore implements IClusterJobStore {

    @Inject
    private JobDao jobDao;
    @Inject
    private KeyStoreDao keyStoreDao;
    @Inject
    private CacheDao cacheDao;
    @Inject
    private Configs configs;
    @Inject
    private ErrorLogger errorLogger;

    private String nodeId;
    private int jobQueueSize; // take job limit

    @Override
    public boolean open() {
        try {
            jobDao.init();
            keyStoreDao.init();
            jobDao.createTables();
            keyStoreDao.createTables();
            nodeId = configs.getConfig("scoopi.cluster.nodeId");
            jobQueueSize = Integer
                    .parseInt(configs.getConfig("scoopi.cluster.jobQueueSize"));
            return true;
        } catch (ConfigNotFoundException | SQLException
                | NumberFormatException e) {
            throw new CriticalException(e);
        }
    }

    @Override
    public boolean close() {
        jobDao.close();
        keyStoreDao.close();
        return true;
    }

    @Override
    public boolean createTables() {
        try {
            jobDao.createTables();
            keyStoreDao.createTables();
        } catch (final Exception e) {
            throw new CriticalException("unable to init Ignite Data Grid", e);
        }
        return true;
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        try {
            return jobDao.putJob(payload);
        } catch (SQLException e) {
            String message =
                    spaceit("put job", payload.getJobInfo().toString());
            errorLogger.log(CAT.ERROR, message, e);
            return false;
        }
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        try {
            return jobDao.takeJob(nodeId.toString());
        } catch (SQLException e) {
            String message = "take job";
            throw new CriticalException(message, e);
        }
    }

    @Override
    public boolean markFinished(final long id) {
        try {
            return jobDao.markFinished(id);
        } catch (SQLException e) {
            String message = spaceit("mark finish job id", String.valueOf(id));
            throw new CriticalException(message, e);
        }
    }

    @Override
    public int getJobCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getJobTakenCount() {
        try {
            return jobDao.getJobTakenCount(nodeId);
        } catch (SQLException e) {
            String message = "get job count";
            throw new CriticalException(message, e);
        }
    }

    @Override
    public int getJobQueueSize() {
        return jobQueueSize;
    }

    @Override
    public boolean isDone() {
        // pending = NEW + TAKEN
        try {
            return jobDao.getPendingJobCount() == 0;
        } catch (SQLException e) {
            String message = "is done";
            throw new CriticalException(message, e);
        }
    }

    @Override
    public State getState() {
        try {
            return State.valueOf(keyStoreDao.getValue("data_grid_state"));
        } catch (NoSuchElementException e) {
            return State.NEW;
        }
    }

    /**
     * insert or replace
     */
    @Override
    public void setState(final State state) {
        keyStoreDao.putValue("data_grid_state", state.toString());
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public boolean changeStateToInitialize() {
        try {
            return keyStoreDao.changeValue("data_grid_state",
                    State.NEW.toString(), State.INITIALIZE.toString());
        } catch (SQLException e) {
            throw new CriticalException("jobStore change state to initialize",
                    e);
        }
    }

    @Override
    public long getJobIdSeq() {
        return cacheDao.getJobIdSeq();
    }

}
