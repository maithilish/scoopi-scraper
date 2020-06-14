package org.codetab.scoopi.store.cluster.hz;

import java.util.Arrays;
import java.util.Stack;

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

    private Stack<String> crashedMembers = new Stack<>();

    public Stack<String> getCrashedMembers() {
        return crashedMembers;
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        String addedMemberId = membershipEvent.getMember().getUuid();
        LOGGER.info("member joined cluster {}", addedMemberId);
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String crashedMemberId = membershipEvent.getMember().getUuid();
        LOGGER.info("member {} left cluster", crashedMemberId);
        crashedMembers.push(crashedMemberId);
        LOGGER.debug("crashed members {}",
                Arrays.toString(crashedMembers.toArray()));
    }

    @Override
    public void memberAttributeChanged(
            final MemberAttributeEvent memberAttributeEvent) {
    }
}
