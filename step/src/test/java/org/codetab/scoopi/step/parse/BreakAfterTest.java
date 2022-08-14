package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.defs.IItemDef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BreakAfterTest {
    @InjectMocks
    private BreakAfter breakAfter;

    @Mock
    private IItemDef itemDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCheck() {
        List<String> list = new ArrayList<>();
        Optional<List<String>> breakAfters = Optional.of(list);

        String value = "Foo";
        list.add(value);

        boolean actual = breakAfter.check(breakAfters, value);

        assertTrue(actual);
    }

    @Test
    public void testGetBreakAfters() {
        String dataDef = "Foo";
        String itemName = "Bar";
        Optional<List<String>> apple = Optional.empty();

        when(itemDef.getBreakAfter(dataDef, itemName)).thenReturn(apple);

        Optional<List<String>> actual =
                breakAfter.getBreakAfters(dataDef, itemName);

        assertEquals(apple, actual);
    }
}
