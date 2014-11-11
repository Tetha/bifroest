package com.goodgame.profiling.rewrite_framework.drain;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainWrapperFactory;

@MetaInfServices
public class FilterUDrainFactory<E extends Environment> implements DrainWrapperFactory<E> {
    @Override
    public String handledType() {
        return "filter-u";
    }

    @Override
    public Drain wrap(E environment, Drain inner, JSONObject config) {
        return new FilterUDrain(inner);
    }

	@Override
	public List<Class<? super E>> getRequiredEnvironments() {
		return Collections.<Class<? super E>>emptyList();
	}
}
