package org.codetab.scoopi.store.cluster.hz;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;

public class MembershipListenerTest {
    @InjectMocks
    private MembershipListener membershipListener;

    @Mock
    private CrashedMembers crashedMembers;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMemberAdded() {
        MembershipEvent membershipEvent = Mockito.mock(MembershipEvent.class);
        Member apple = Mockito.mock(Member.class);
        UUID grape = Mockito.mock(UUID.class);
        String addedMemberId = "Foo";

        when(membershipEvent.getMember()).thenReturn(apple);
        when(apple.getUuid()).thenReturn(grape);
        when(grape.toString()).thenReturn(addedMemberId);
        membershipListener.memberAdded(membershipEvent);
    }

    @Test
    public void testMemberRemoved() {
        MembershipEvent membershipEvent = Mockito.mock(MembershipEvent.class);
        Member apple = Mockito.mock(Member.class);
        UUID grape = Mockito.mock(UUID.class);
        String crashedMemberId = "Foo";

        when(membershipEvent.getMember()).thenReturn(apple);
        when(apple.getUuid()).thenReturn(grape);
        when(grape.toString()).thenReturn(crashedMemberId);
        membershipListener.memberRemoved(membershipEvent);

        verify(crashedMembers).add(crashedMemberId);
    }
}

