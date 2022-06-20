package org.codetab.scoopi.store.cluster.hz;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class CrashedMembersTest {
    @InjectMocks
    private CrashedMembers crashedMembers;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSet() throws Exception {
        assertTrue(FieldUtils.readDeclaredField(crashedMembers, "members",
                true) instanceof CopyOnWriteArraySet);
    }

    @Test
    public void testAdd() {
        String crashedMember = "Foo";
        crashedMembers.add(crashedMember);
    }

    @Test
    public void testContains() {
        String crashedMember = "Foo";
        assertFalse(crashedMembers.contains(crashedMember));

        crashedMembers.add(crashedMember);
        assertTrue(crashedMembers.contains(crashedMember));
    }

    @Test
    public void testIsEmpty() {

        boolean actual = crashedMembers.isEmpty();

        assertTrue(actual);
    }

    @Test
    public void testDifference() {
        crashedMembers.add("foo");
        crashedMembers.add("bar");
        Set<String> other = new HashSet<>();
        other.add("bar");
        other.add("Quz");

        Set<String> actual = crashedMembers.difference(other);

        assertEquals(1, actual.size());
        assertTrue(actual.contains("foo"));
    }

    @Test
    public void testToArray() {
        Object[] apple = {"foo"};

        crashedMembers.add("foo");
        Object[] actual = crashedMembers.toArray();

        assertArrayEquals(apple, actual);
    }
}

