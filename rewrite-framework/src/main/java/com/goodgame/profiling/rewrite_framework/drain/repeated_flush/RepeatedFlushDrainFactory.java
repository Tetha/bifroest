package com.goodgame.profiling.rewrite_framework.drain.repeated_flush;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.units.parse.DurationParser;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainWrapperFactory;

@MetaInfServices
public class RepeatedFlushDrainFactory<E extends EnvironmentWithTaskRunner> implements DrainWrapperFactory<E> {
    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.asList( EnvironmentWithTaskRunner.class );
    }

    @Override
    public String handledType() {
        return "repeated-flush";
    }

    @Override
    public Drain wrap( E environment, Drain inner, JSONObject subconfiguration ) {
        return new RepeatedFlushDrain( inner,
                                       environment.taskRunner(),
                                       new DurationParser().parse( subconfiguration.getString( "each" ) ) );
    }
}
