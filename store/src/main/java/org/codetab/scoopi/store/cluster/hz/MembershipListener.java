package org.codetab.scoopi.store.cluster.hz;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.cluster.MembershipEvent;

@Singleton
public class MembershipListener
        implements com.hazelcast.cluster.MembershipListener {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private CrashedMembers crashedMembers;

    @Override
    public void memberAdded(final MembershipEvent membershipEvent) {
        String addedMemberId = membershipEvent.getMember().getUuid().toString();
        LOG.info("member {} joined the cluster", addedMemberId);
    }

    @Override
    public void memberRemoved(final MembershipEvent membershipEvent) {
        String crashedMemberId =
                membershipEvent.getMember().getUuid().toString();
        LOG.info("member {} left the cluster", crashedMemberId);
        crashedMembers.add(crashedMemberId);
    }

}
