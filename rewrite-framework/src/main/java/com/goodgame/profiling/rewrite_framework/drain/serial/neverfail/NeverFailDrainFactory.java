package com.goodgame.profiling.rewrite_framework.drain.serial.neverfail;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainMultipleWrapperFactory;

@MetaInfServices
public class NeverFailDrainFactory<E extends Environment> implements DrainMultipleWrapperFactory<E> {
    @Override
    public String handledType() {
        return "never-fail";
    }

    @Override
    public Drain wrap( E environment, List<Drain> inners, JSONObject config ) {
        return new SerialNeverFailDrain( inners );
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.emptyList();
    }
}
