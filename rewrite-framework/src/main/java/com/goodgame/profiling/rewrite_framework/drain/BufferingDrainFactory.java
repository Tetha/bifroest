package com.goodgame.profiling.rewrite_framework.drain;

import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.parse.TimeUnitParser;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainWrapperFactory;

@MetaInfServices
public class BufferingDrainFactory<E extends Environment> implements DrainWrapperFactory<E> {
    @Override
    public String handledType() {
        return "buffered";
    }

    @Override
    public Drain wrap(E environment, Drain inner, JSONObject config) {
        long warnlimit;
        try {
            warnlimit = config.getLong("warnlimit") * 1000;
        } catch (JSONException e) {
            String warn = config.getString("warnlimit");
            warnlimit = (new TimeUnitParser(SI_PREFIX.MILLI, TIME_UNIT.SECOND)).parse(warn).longValue();
        }

        return new BufferingDrain(
                config.getInt("buffersize"),
                warnlimit,
                inner
        );
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.<Class<? super E>> emptyList();
    }
}
