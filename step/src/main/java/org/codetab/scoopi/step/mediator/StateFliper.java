package org.codetab.scoopi.step.mediator;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.store.IShutdown;

import com.google.inject.Singleton;

@Singleton
public class StateFliper {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IShutdown shutdown;

    private AtomicReference<TMState> tmState =
            new AtomicReference<>(TMState.READY);

    public void cancel() {
        shutdown.cancel();
    }

    public boolean tryTMShutdown() {
        LOG.info("task mediator done, try shutdown");
        shutdown.setDone();

        // if (cancelled.get()) {
        // don't consult jobStore to get pending job count
        // return shutdownFunction.apply(this);
        // } else {
        // }

        // shutdown consults jobStore and shuts only if no job pending
        if (shutdown.tryShutdown(shutdownFunction, this)) {
            LOG.info("task mediator shutdown successful");
            return true;
        } else {
            tmState.set(TMState.READY);
            LOG.info(
                    "task mediator shutdown failed, reset state back to ready");
            return false;
        }

    }

    private Function<StateFliper, Boolean> shutdownFunction = tm -> {
        if (tmState.get().equals(TMState.DONE)) {
            tmState.set(TMState.SHUTDOWN);
            return true;
        } else {
            return false;
        }
    };

    public TMState getTMState() {
        return tmState.get();
    }

    public void setTMState(final TMState state) {
        this.tmState.set(state);
    }

    public boolean isTMState(final TMState other) {
        return tmState.get().equals(other);
    }
}
