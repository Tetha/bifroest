package com.goodgame.profiling.rewrite_framework.core.config;

import java.util.Collection;

import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainCreator;
import com.goodgame.profiling.rewrite_framework.core.output.RewriteRule;
import com.goodgame.profiling.rewrite_framework.core.output.RewriteRuleCreator;
import com.goodgame.profiling.rewrite_framework.core.source.SourceSet;
import com.goodgame.profiling.rewrite_framework.core.source.SourceSetCreator;

public final class FetchConfigurationFromConfiguration< E extends EnvironmentWithJSONConfiguration, I, U > implements FetchConfiguration<I, U> {

    private final Collection<SourceSet<U>> sources;
    private final Drain drain;
    private final Collection<RewriteRule<I>> rules;

    public FetchConfigurationFromConfiguration( Class<I> inputClass, Class<U> unitClass, E environment, JSONObject config ) {
        sources = new SourceSetCreator<>( unitClass ).loadConfiguration( environment, config );
        drain = new DrainCreator<>().loadConfiguration( environment, config );
        rules = new RewriteRuleCreator<>( inputClass ).loadConfiguration( environment, config );
    }

    @Override
    public Collection<SourceSet<U>> sources() {
        return sources;
    }

    @Override
    public Drain drain() {
        return drain;
    }

    @Override
    public Collection<RewriteRule<I>> rules() {
        return rules;
    }

    @Override
    public String toString() {
        return "FetchConfigurationFromConfiguration [sources=" + sources + ", drain=" + drain + ", rules=" + rules + "]";
    }
}
