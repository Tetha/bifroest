package com.goodgame.profiling.rewrite_framework.drain.empty;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.drain.BasicDrainFactory;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

@MetaInfServices
public class EmptyDrainFactory<E extends Environment> implements BasicDrainFactory<E> {

	@Override
	public String handledType() {
		return "void";
	}

	@Override
	public Drain create( E environment, JSONObject config ) {
		return new EmptyDrain();
	}

	@Override
	public List<Class<? super E>> getRequiredEnvironments() {
		return Collections.<Class<? super E>>emptyList();
	}
}
