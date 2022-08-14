package org.codetab.scoopi.step.parse;

import static org.junit.Assert.assertEquals;
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

public class PrefixProcessorTest {
    @InjectMocks
    private PrefixProcessor prefixProcessor;

    @Mock
    private IItemDef itemDef;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPrefixes() {
        String dataDef = "Foo";
        String itemName = "Bar";
        Optional<List<String>> apple = Optional.empty();

        when(itemDef.getPrefix(dataDef, itemName)).thenReturn(apple);

        Optional<List<String>> actual =
                prefixProcessor.getPrefixes(dataDef, itemName);

        assertEquals(apple, actual);
    }

    @Test
    public void testPrefixValue() {
        String value = "Foo";
        List<String> prefixes = new ArrayList<>();
        prefixes.add("Bar");

        String apple = "BarFoo";

        String actual = prefixProcessor.prefixValue(value, prefixes);

        assertEquals(apple, actual);
    }
}
