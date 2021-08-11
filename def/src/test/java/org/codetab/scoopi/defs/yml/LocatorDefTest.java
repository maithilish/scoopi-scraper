package org.codetab.scoopi.defs.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class LocatorDefTest {

    @InjectMocks
    private LocatorDef locatorDef;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LocatorDefData data;

    private ObjectFactory of;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        of = new ObjectFactory();
    }

    @Test
    public void testGetGroups() {
        List<String> groups = Lists.newArrayList("foo", "bar");

        when(data.getGroupNames()).thenReturn(groups);

        List<String> actual = locatorDef.getGroups();

        assertThat(actual).isEqualTo(groups);
        assertThat(actual.getClass().getSimpleName())
                .isEqualTo("UnmodifiableRandomAccessList");
    }

    @Test
    public void testGetLocatorGroup() {
        String group = "tgroup";
        Locator locator = of.createLocator("foo", group, "bar");
        LocatorGroup locatorGroup = of.createLocatorGroup(group);
        locatorGroup.getLocators().add(locator);
        List<LocatorGroup> locatorGroups = Lists.newArrayList(locatorGroup);

        when(data.getLocatorGroups()).thenReturn(locatorGroups)
                .thenReturn(Lists.newArrayList());

        LocatorGroup actual = locatorDef.getLocatorGroup(group).get();

        assertThat(actual).isEqualTo(locatorGroup);
        assertThat(actual).isNotSameAs(locatorGroup);

        // locatorGroup not found
        assertThat(locatorDef.getLocatorGroup(group)).isNotPresent();
    }

    @Test
    public void testGetLocatorGroups() {
        String group = "tgroup";
        Locator locator = of.createLocator("foo", group, "bar");
        LocatorGroup locatorGroup = of.createLocatorGroup(group);
        locatorGroup.getLocators().add(locator);
        List<LocatorGroup> locatorGroups = Lists.newArrayList(locatorGroup);

        when(data.getLocatorGroups()).thenReturn(locatorGroups);

        List<LocatorGroup> actual = locatorDef.getLocatorGroups();

        assertThat(actual).isSameAs(locatorGroups);
    }

}
