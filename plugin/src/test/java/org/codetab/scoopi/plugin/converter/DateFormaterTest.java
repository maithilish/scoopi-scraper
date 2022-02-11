package org.codetab.scoopi.plugin.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.model.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DateFormaterTest {
    @InjectMocks
    private DateFormater dateFormater;

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
        String input = "2037-12-31T23:59:59.999+0530";
        String inPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        ZonedDateTime date = Mockito.mock(ZonedDateTime.class);
        String outPattern = "dd-MM-yyyy'T'HH:mm:ss.SSSZ";
        DateTimeFormatter outFormatter =
                DateTimeFormatter.ofPattern(outPattern);
        String output = "31-12-2037T23:59:59.999+0530";

        when(pluginDef.getValue(plugin, "inPattern")).thenReturn(inPattern);
        when(pluginDef.getValue(plugin, "outPattern")).thenReturn(outPattern);
        when(date.format(outFormatter)).thenReturn(output);

        String actual = dateFormater.convert(input);

        assertEquals(output, actual);
        verify(plugin).put(input, output);
    }

    @Test
    public void testConvertAlreadyConverted() throws Exception {
        String input = "2037-12-31T23:59:59.999+0530";
        String inPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        ZonedDateTime date = Mockito.mock(ZonedDateTime.class);
        String outPattern = "dd-MM-yyyy'T'HH:mm:ss.SSSZ";
        DateTimeFormatter outFormatter =
                DateTimeFormatter.ofPattern(outPattern);
        String output = "31-12-2037T23:59:59.999+0530";

        when(plugin.get(input)).thenReturn(output);
        when(pluginDef.getValue(plugin, "inPattern")).thenReturn(inPattern);
        when(pluginDef.getValue(plugin, "outPattern")).thenReturn(outPattern);
        when(date.format(outFormatter)).thenReturn(output);

        String actual = dateFormater.convert(input);

        assertEquals(output, actual);
        verify(plugin, never()).put(input, output);
    }

    @Test
    public void testConvertParseException() throws Exception {
        String input = "invalid date";
        String inPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        ZonedDateTime date = Mockito.mock(ZonedDateTime.class);
        String outPattern = "dd-MM-yyyy'T'HH:mm:ss.SSSZ";
        DateTimeFormatter outFormatter =
                DateTimeFormatter.ofPattern(outPattern);
        String output = "31-12-2037T23:59:59.999+0530";

        when(pluginDef.getValue(plugin, "inPattern")).thenReturn(inPattern);
        when(pluginDef.getValue(plugin, "outPattern")).thenReturn(outPattern);
        when(date.format(outFormatter)).thenReturn(output);

        assertThrows(DateTimeParseException.class,
                () -> dateFormater.convert(input));

        verify(plugin, never()).put(input, output);
    }

    @Test
    public void testSetPlugin() throws IllegalAccessException {
        Plugin p = Mockito.mock(Plugin.class);
        dateFormater.setPlugin(p);
        assertSame(p,
                FieldUtils.readDeclaredField(dateFormater, "plugin", true));
    }
}
