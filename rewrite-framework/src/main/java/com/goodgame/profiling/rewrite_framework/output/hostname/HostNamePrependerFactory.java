package com.goodgame.profiling.rewrite_framework.output.hostname;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;
import com.goodgame.profiling.rewrite_framework.core.output.generator.WrappingNameGeneratorFactory;

@MetaInfServices
public class HostNamePrependerFactory<E extends Environment>
implements WrappingNameGeneratorFactory<E> {
    @Override
    public String handledType() {
        return "hostname";
    }

    @Override
    public MetricNameGenerator wrap( E environment, MetricNameGenerator inner, JSONObject configuration ) {
        return new HostNamePrepender( inner );
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.emptyList();
    }
}
