package org.codetab.scoopi.store.cluster.hz;

import javax.inject.Singleton;

import org.codetab.scoopi.store.IJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;

@Singleton
public class MembershipListener
        implements com.hazelcast.core.MembershipListener {

    static final Logger LOGGER =
            LoggerFactory.getLogger(MembershipListener.class);

    private IJobStore jobStore;

    public void setJobStore(final IJobStore jobStore) {
        this.jobStore = jobStore;
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        LOGGER.info("Member joined cluster {}",
                membershipEvent.getMember().getUuid());
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String memberId = membershipEvent.getMember().getUuid();
        LOGGER.info("Member {} left cluster, reset taken jobs", memberId);
        jobStore.resetTakenJobs(memberId);
    }

    @Override
    public void memberAttributeChanged(
            final MemberAttributeEvent memberAttributeEvent) {
    }

}
