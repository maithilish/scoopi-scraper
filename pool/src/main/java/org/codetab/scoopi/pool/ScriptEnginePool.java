package org.codetab.scoopi.pool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptEngine;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.codetab.scoopi.model.Plugin;

@Singleton
public class ScriptEnginePool
        extends GenericKeyedObjectPool<Plugin, ScriptEngine> {

    @Inject
    public ScriptEnginePool(final ScriptEngineFactory factory) {
        super(factory);
    }
}
