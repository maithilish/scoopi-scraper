package org.codetab.scoopi.plugin.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DateRollerTest {
    @InjectMocks
    private DateRoller dateRoller;

    @Mock
    private IPluginDef pluginDef;
    @Mock
    private Plugin plugin;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConvert() throws Exception {
        String input = "2020-02-20T20:40:59.999+0530";
        String inPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        String outPattern = "dd-MM-yyyy'T'HH:mm:ss.SSSZ";
        String rollStr = "DAY_OF_MONTH=ceil HOUR=floor MINUTE=round SECOND=20";
        String output = "29-02-2020T12:59:20.999+0530";

        when(pluginDef.getValue(plugin, "inPattern")).thenReturn(inPattern);
        when(pluginDef.getValue(plugin, "outPattern")).thenReturn(outPattern);
        when(pluginDef.getValue(plugin, "roll")).thenReturn(rollStr);

        String actual = dateRoller.convert(input);

        assertEquals(output, actual);
        verify(plugin).put(input, output);
    }

    @Test
    public void testConvertRoundLessThanMid() throws Exception {
        String input = "2037-12-31T23:25:59.999+0530";
        String inPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        String outPattern = "dd-MM-yyyy'T'HH:mm:ss.SSSZ";
        String rollStr = "MINUTE=round";
        String output = "31-12-2037T23:00:59.999+0530";

        when(pluginDef.getValue(plugin, "inPattern")).thenReturn(inPattern);
        when(pluginDef.getValue(plugin, "outPattern")).thenReturn(outPattern);
        when(pluginDef.getValue(plugin, "roll")).thenReturn(rollStr);

        String actual = dateRoller.convert(input);

        assertEquals(output, actual);
        verify(plugin).put(input, output);
    }

    @Test
    public void testConvertAlreadyConverted() throws Exception {
        String input = "2037-12-31T23:59:59.999+0530";
        String inPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        String outPattern = "dd-MM-yyyy'T'HH:mm:ss.SSSZ";
        String rollStr = "DAY_OF_MONTH=ceil";
        String output = "31-12-2037T23:59:59.999+0530";

        when(plugin.get(input)).thenReturn(output);
        when(pluginDef.getValue(plugin, "inPattern")).thenReturn(inPattern);
        when(pluginDef.getValue(plugin, "outPattern")).thenReturn(outPattern);
        when(pluginDef.getValue(plugin, "roll")).thenReturn(rollStr);

        String actual = dateRoller.convert(input);

        assertEquals(output, actual);
        verify(plugin, never()).put(input, output);
    }

    @Test
    public void testSetPlugin() throws IllegalAccessException {
        Plugin p = Mockito.mock(Plugin.class);
        dateRoller.setPlugin(p);
        assertSame(p, FieldUtils.readDeclaredField(dateRoller, "plugin", true));
    }
}
