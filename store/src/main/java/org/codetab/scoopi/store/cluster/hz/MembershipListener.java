package org.codetab.scoopi.store.cluster.hz;

import java.util.Arrays;
import java.util.Stack;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.cluster.MembershipEvent;

@Singleton
public class MembershipListener
        implements com.hazelcast.cluster.MembershipListener {

    private static final Logger LOG = LogManager.getLogger();

    private Stack<String> crashedMembers = new Stack<>();

    public Stack<String> getCrashedMembers() {
        return crashedMembers;
    }

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        String addedMemberId = membershipEvent.getMember().getUuid().toString();
        LOG.info("member joined cluster {}", addedMemberId);
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String crashedMemberId =
                membershipEvent.getMember().getUuid().toString();
        LOG.info("member {} left cluster", crashedMemberId);
        crashedMembers.push(crashedMemberId);
        LOG.debug("crashed members {}",
                Arrays.toString(crashedMembers.toArray()));
    }
}
