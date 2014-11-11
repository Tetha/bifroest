package com.goodgame.profiling.graphite_bifroest.commands;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.graphite_bifroest.systems.rebuilder.EnvironmentWithTreeRebuilder;

@MetaInfServices
public final class ForceRecalculationCommand< E extends EnvironmentWithTreeRebuilder > implements Command<E> {

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.<Class<? super E>> singletonList( EnvironmentWithTreeRebuilder.class );
    }

    @Override
    public String getJSONCommand() {
        return "force-recalculation";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        environment.getRebuilder().rebuild();
        return new JSONObject();
    }

}
