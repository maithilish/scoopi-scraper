package org.codetab.scoopi.step.process;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LocatorGroupFactoryTest {
    @InjectMocks
    private LocatorGroupFactory locatorGroupFactory;

    @Mock
    private IItemDef itemDef;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private Errors errors;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateLocatorGroupsIfOLinkBreakOnGetContainsIfStringUtilsIsNotBlankIf() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";

        String grape = "Qux";
        Optional<List<String>> oLinkBreakOn = Optional.empty();

        String linkGroup = "Quux";
        Optional<String> oLinkGroup = Optional.of(linkGroup);

        String url = "Corge";
        Locator locator = Mockito.mock(Locator.class);
        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);
        List<Locator> list2 = new ArrayList<>();
        Axis axis2 = Mockito.mock(Axis.class);

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        arrayList.add(lg);

        items.add(item);

        when(item.getFirstAxis()).thenReturn(axis);
        when(axis.getItemName()).thenReturn(apple).thenReturn(grape);
        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(axis.getValue()).thenReturn(url);
        when(objectFactory.createLocator(locatorName, linkGroup, url))
                .thenReturn(locator);
        when(objectFactory.createLocatorGroup(linkGroup)).thenReturn(lg);
        when(lg2.getLocators()).thenReturn(list2);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);

        verify(lg).setByDef(false);
        verify(item, never()).getAxes();
        verify(axis2, never()).getItemName();
        verify(errors, never()).inc();
    }

    @Test
    public void testCreateLocatorGroupsIfOLinkBreakOnGetContainsIfStringUtilsIsNotBlankElse() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";

        String grape = "Qux";
        Optional<List<String>> oLinkBreakOn = Optional.empty();

        String linkGroup = "Quux";
        Optional<String> oLinkGroup = Optional.of(linkGroup);

        String url = ""; // isNotBlankElse then url is blank
        Locator locator = Mockito.mock(Locator.class);
        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);
        List<Locator> list2 = new ArrayList<>();
        Axis axis2 = Mockito.mock(Axis.class);

        items.add(item);

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        // arrayList.add(lg); ---- if url blank list is empty

        when(item.getFirstAxis()).thenReturn(axis);
        when(axis.getItemName()).thenReturn(apple).thenReturn(grape);
        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(axis.getValue()).thenReturn(url);
        when(objectFactory.createLocator(locatorName, linkGroup, url))
                .thenReturn(locator);
        when(lg2.getLocators()).thenReturn(list2);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);

        verify(objectFactory, never()).createLocatorGroup(linkGroup);
        verify(lg, never()).setByDef(false);
        verify(item, never()).getAxes();
        verify(axis2, never()).getItemName();
        verify(errors, never()).inc();
    }

    @Test
    public void testCreateLocatorGroupsIfOLinkBreakOnIsPresentIfOLinkBreakOnGetContainsElseStringUtilsIsNotBlank() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";

        String linkGroup = "Quux";
        Optional<String> oLinkGroup = Optional.of(linkGroup);

        String grape = "Qux";

        // IfOLinkBreakOnGetContains then createLink false
        String url = "Corge";
        List<String> urlList = new ArrayList<>();
        urlList.add(url);
        Optional<List<String>> oLinkBreakOn = Optional.of(urlList);

        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);
        Axis axis2 = Mockito.mock(Axis.class);

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        // arrayList.add(lg); ---- if createLink false list is empty

        items.add(item);

        when(item.getFirstAxis()).thenReturn(axis);
        when(axis.getItemName()).thenReturn(apple).thenReturn(grape);
        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(axis.getValue()).thenReturn(url);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);

        verify(objectFactory, never()).createLocator(locatorName, linkGroup,
                url);
        verify(objectFactory, never()).createLocatorGroup(linkGroup);
        verify(lg, never()).setByDef(false);
        verify(lg2, never()).getLocators();
        verify(item, never()).getAxes();
        verify(axis2, never()).getItemName();
        verify(errors, never()).inc();
    }

    @Test
    public void testCreateLocatorGroupsIfOLinkGroupIsPresentIfOLinkBreakOnIsPresentElseOLinkBreakOnGetContains() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";

        String grape = "Qux";
        String linkGroup = "Quux";
        Optional<String> oLinkGroup = Optional.of(linkGroup);

        Locator locator = Mockito.mock(Locator.class);

        // ElseOLinkBreakOnGetContains then createLink is true
        String url = "Corge";
        List<String> urlList = new ArrayList<>();
        // ElseOLinkBreakOnGetContains means no url in oLinkBreakOn
        // urlList.add(url);
        Optional<List<String>> oLinkBreakOn = Optional.of(urlList);

        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);

        List<Locator> list2 = new ArrayList<>();
        Axis axis2 = Mockito.mock(Axis.class);

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        arrayList.add(lg); // if createLink is true list is not empty

        items.add(item);

        when(item.getFirstAxis()).thenReturn(axis);
        when(axis.getItemName()).thenReturn(apple).thenReturn(grape);
        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(axis.getValue()).thenReturn(url);
        when(objectFactory.createLocator(locatorName, linkGroup, url))
                .thenReturn(locator);
        when(objectFactory.createLocatorGroup(linkGroup)).thenReturn(lg);
        when(lg2.getLocators()).thenReturn(list2);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);

        verify(lg).setByDef(false);
        verify(item, never()).getAxes();
        verify(axis2, never()).getItemName();
        verify(errors, never()).inc();
    }

    @Test
    public void testCreateLocatorGroupsIfOLinkGroupIsPresentElseOLinkBreakOnIsPresent() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";

        String grape = "Qux";

        String linkGroup = "Quux";
        Optional<String> oLinkGroup = Optional.of(linkGroup);

        String url = "Corge";
        // ElseOLinkBreakOnIsPresent
        Optional<List<String>> oLinkBreakOn = Optional.empty();

        Locator locator = Mockito.mock(Locator.class);
        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);

        List<Locator> list2 = new ArrayList<>();
        Axis axis2 = Mockito.mock(Axis.class);

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        // if url is not blank and createLink then list not empty
        arrayList.add(lg);

        items.add(item);

        when(item.getFirstAxis()).thenReturn(axis);
        when(axis.getItemName()).thenReturn(apple).thenReturn(grape);
        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(axis.getValue()).thenReturn(url);
        when(objectFactory.createLocator(locatorName, linkGroup, url))
                .thenReturn(locator);
        when(objectFactory.createLocatorGroup(linkGroup)).thenReturn(lg);
        when(lg2.getLocators()).thenReturn(list2);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);
        verify(lg).setByDef(false);
        verify(item, never()).getAxes();
        verify(axis2, never()).getItemName();
        verify(errors, never()).inc();
    }

    @Test
    public void testCreateLocatorGroupsElseOLinkGroupIsPresent() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";
        Optional<String> oLinkGroup = Optional.empty(); // not present
        String grape = "Qux";
        Optional<List<String>> oLinkBreakOn = Optional.empty();
        String linkGroup = "Quux";
        String url = "Corge";
        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);
        List<Axis> list3 = new ArrayList<>();
        Axis axis2 = Mockito.mock(Axis.class);
        String fig = "Grault";

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        // oLinkGroup not present then list is empty
        // arrayList.add(lg);

        items.add(item);
        list3.add(axis2);

        when(item.getFirstAxis()).thenReturn(axis);
        when(axis.getItemName()).thenReturn(apple).thenReturn(grape);
        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(item.getAxes()).thenReturn(list3);
        when(axis2.getItemName()).thenReturn(fig);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);

        verify(axis, never()).getValue();
        verify(objectFactory, never()).createLocator(locatorName, linkGroup,
                url);
        verify(objectFactory, never()).createLocatorGroup(linkGroup);
        verify(lg, never()).setByDef(false);
        verify(lg2, never()).getLocators();
        verify(errors).inc();
    }

    @Test
    public void testCreateLocatorGroupsLgsContainsKey() {
        String dataDef = "Foo";
        List<Item> items = new ArrayList<>();
        String locatorName = "Bar";
        Item item = Mockito.mock(Item.class);
        Axis axis = Mockito.mock(Axis.class);
        String apple = "Baz";

        String grape = "Qux";
        Optional<List<String>> oLinkBreakOn = Optional.empty();

        String linkGroup = "Quux";
        Optional<String> oLinkGroup = Optional.of(linkGroup);

        String url = "Corge";
        Locator locator = Mockito.mock(Locator.class);
        LocatorGroup lg = Mockito.mock(LocatorGroup.class);
        LocatorGroup lg2 = Mockito.mock(LocatorGroup.class);
        List<Locator> list2 = new ArrayList<>();
        Axis axis2 = Mockito.mock(Axis.class);

        ArrayList<LocatorGroup> arrayList = new ArrayList<>();
        arrayList.add(lg);

        items.add(item);

        // loop second time to test lgs.containsKey()
        Item item2 = Mockito.mock(Item.class);
        items.add(item2);

        when(item.getFirstAxis()).thenReturn(axis);

        when(item2.getFirstAxis()).thenReturn(axis);

        when(axis.getItemName()).thenReturn(apple).thenReturn(grape)
                .thenReturn(apple).thenReturn(grape);

        when(itemDef.getLinkGroup(dataDef, apple)).thenReturn(oLinkGroup);
        when(itemDef.getLinkBreakOn(dataDef, grape)).thenReturn(oLinkBreakOn);
        when(axis.getValue()).thenReturn(url);
        when(objectFactory.createLocator(locatorName, linkGroup, url))
                .thenReturn(locator);
        when(objectFactory.createLocatorGroup(linkGroup)).thenReturn(lg);
        when(lg2.getLocators()).thenReturn(list2);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef, items, locatorName);

        assertEquals(arrayList, actual);

        verify(lg).setByDef(false);
        verify(item, never()).getAxes();
        verify(axis2, never()).getItemName();
        verify(errors, never()).inc();
    }

}
