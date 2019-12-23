package org.codetab.scoopi.store.cluster.ignite;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.store.cluster.IClusterJobStore;
import org.codetab.scoopi.store.cluster.ignite.dao.JobDao;
import org.codetab.scoopi.store.cluster.ignite.dao.KeyStoreDao;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Singleton
public class JobStore implements IClusterJobStore {

    @Inject
    private JobDao jobDao;
    @Inject
    private KeyStoreDao keyStoreDao;

    @Override
    public boolean init() {
        jobDao.initJdbi();
        keyStoreDao.initJdbi();
        return true;
    }

    @Override
    public boolean createTables() {
        try {
            jobDao.createJobTable();
            keyStoreDao.createKeyStoreTable();
        } catch (Exception e) {
            throw new CriticalException("unable to init Ignite Data Grid", e);
        }
        return true;
    }

    @Override
    public boolean putJob(final Payload payload) throws InterruptedException {
        jobDao.putJob(payload);
        return true;
    }

    @Override
    public Payload takeJob() throws InterruptedException {
        return jobDao.takeJob();
    }

    @Override
    public boolean markFinished(final long id) {
        return jobDao.markFinished(id);
    }

    @Override
    public int getJobCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isDone() {
        int pendingJobs = jobDao.getPendingJobsCount();
        return pendingJobs == 0;
    }

    @Override
    public State getState() {
        try {
            return State.valueOf(keyStoreDao.getValue("data_grid_state"));
        } catch (UnableToExecuteStatementException e) {
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
    public boolean changeStateToInitialize() {
        String s = keyStoreDao.changeValue("data_grid_state",
                State.NEW.toString(), State.INITIALIZE.toString());
        return s.equals(State.INITIALIZE.toString());
    }
}
