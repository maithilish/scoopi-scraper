package org.codetab.scoopi.store.cluster.hz;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

/**
 * Set of all crashed members during lifetime of cluster. By design, it is not
 * allowed to remove members from the set. Clients should use difference method
 * to get set of members that are pending to be processed by them.
 *
 * @author m
 *
 */
@Singleton
public class CrashedMembers {

    private static final Logger LOG = LogManager.getLogger();

    private Set<String> members = new CopyOnWriteArraySet<>();

    public void add(final String crashedMember) {
        members.add(crashedMember);
        LOG.debug("crashed members {}", Arrays.toString(members.toArray()));
    }

    public boolean contains(final String crashedMember) {
        return members.contains(crashedMember);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * Get set of members that are in crashedMembers set and not in other set.
     * @param other
     * @return set of members
     */
    public Set<String> difference(final Set<String> other) {
        return Sets.difference(members, other);
    }

    public Object[] toArray() {
        return members.toArray();
    }
}
