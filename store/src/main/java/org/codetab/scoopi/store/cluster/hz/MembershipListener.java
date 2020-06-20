package org.codetab.scoopi.store.cluster.hz;

import java.util.Arrays;
import java.util.Stack;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.MembershipEvent;

@Singleton
public class MembershipListener
        implements com.hazelcast.cluster.MembershipListener {

    static final Logger LOGGER =
            LoggerFactory.getLogger(MembershipListener.class);

    private Stack<String> crashedMembers = new Stack<>();

    public Stack<String> getCrashedMembers() {
        return crashedMembers;
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        String addedMemberId = membershipEvent.getMember().getUuid().toString();
        LOGGER.info("member joined cluster {}", addedMemberId);
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String crashedMemberId =
                membershipEvent.getMember().getUuid().toString();
        LOGGER.info("member {} left cluster", crashedMemberId);
        crashedMembers.push(crashedMemberId);
        LOGGER.debug("crashed members {}",
                Arrays.toString(crashedMembers.toArray()));
    }
}
