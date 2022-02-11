package org.codetab.scoopi.plugin.script;

import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.mockito.Mockito;

public class ScriptEnginePoolTest {

    @Test
    public void testScriptEnginePool() {
        ScriptEngineFactory factory = Mockito.mock(ScriptEngineFactory.class);
        try (ScriptEnginePool scriptEnginePool =
                new ScriptEnginePool(factory)) {
            assertSame(factory, scriptEnginePool.getFactory());
        }
    }
}
