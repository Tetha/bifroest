package com.goodgame.profiling.rewrite_framework.drain.persistent;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.rewrite_framework.core.drain.BasicDrainFactory;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.systems.persistent_drains.EnvironmentWithPersistentDrainManager;

@MetaInfServices
public class PersistentDrainFrontendFactory<E extends EnvironmentWithPersistentDrainManager> implements BasicDrainFactory<E> {
	@Override
	public List<Class<? super E>> getRequiredEnvironments() {
		return Arrays.<Class<? super E>>asList(EnvironmentWithPersistentDrainManager.class);
	}

	@Override
	public String handledType() {
		return "persistent";
	}

	@Override
	public Drain create(E environment, JSONObject config) {
		return new PersistentDrainFrontend(environment.persistentDrainManager().getPersistentDrain(config.getString("id")));
	}
}
