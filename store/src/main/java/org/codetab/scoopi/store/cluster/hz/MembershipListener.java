package org.codetab.scoopi.store.cluster.hz;

import static java.util.Objects.nonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    private Set<String> crashedMembers =
            Collections.synchronizedSet(new HashSet<>());

    public void setCrashCleaner(final CrashCleaner crashCleaner) {
        this.crashCleaner = crashCleaner;
        replayLeftoutCrashes();
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        String addedMemberId = membershipEvent.getMember().getUuid();
        LOGGER.info("Member joined cluster {}", addedMemberId);
        replayLeftoutCrashes();
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String crashedMemberId = membershipEvent.getMember().getUuid();
        if (nonNull(crashCleaner)) {
            crashCleaner.addCrashedMember(crashedMemberId);
            replayLeftoutCrashes();
        } else {
            crashedMembers.add(crashedMemberId);
        }
        // shutdown.removeMember(crashedMemberId);
        LOGGER.info("Member {} left cluster, schedule reset jobs",
                crashedMemberId);
    }

    @Override
    public void memberAttributeChanged(
            final MemberAttributeEvent memberAttributeEvent) {
    }

    // replay any left out crashed members
    private void replayLeftoutCrashes() {
        for (String memberId : crashedMembers) {
            crashCleaner.addCrashedMember(memberId);
            crashedMembers.remove(memberId);
        }
    }
}
