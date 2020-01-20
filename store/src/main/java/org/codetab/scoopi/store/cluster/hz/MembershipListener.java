package org.codetab.scoopi.store.cluster.hz;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;

@Singleton
public class MembershipListener
        implements com.hazelcast.core.MembershipListener {

    static final Logger LOGGER =
            LoggerFactory.getLogger(MembershipListener.class);

    private CrashCleaner crashCleaner;

    public void setCrashCleaner(final CrashCleaner crashCleaner) {
        this.crashCleaner = crashCleaner;
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        LOGGER.info("Member joined cluster {}",
                membershipEvent.getMember().getUuid());
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String crashedMemberId = membershipEvent.getMember().getUuid();
        crashCleaner.addCrashedMember(crashedMemberId);
        LOGGER.info("Member {} left cluster, schedule reset jobs",
                crashedMemberId);
    }

    @Override
    public void memberAttributeChanged(
            final MemberAttributeEvent memberAttributeEvent) {
    }

}
