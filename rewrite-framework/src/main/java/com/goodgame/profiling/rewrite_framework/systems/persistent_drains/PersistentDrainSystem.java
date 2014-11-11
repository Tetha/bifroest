package com.goodgame.profiling.rewrite_framework.systems.persistent_drains;

import java.util.Arrays;
import java.util.Collection;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.util.panic.ProfilingPanic;
import com.goodgame.profiling.rewrite_framework.drain.persistent.DumpPersistentDrainStatus;
import com.goodgame.profiling.rewrite_framework.systems.RewriteIdentifiers;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public class PersistentDrainSystem<E extends EnvironmentWithMutablePersistentDrainManager & EnvironmentWithJSONConfiguration> implements Subsystem<E> {
	@Override
	public String getSystemIdentifier() {
		return RewriteIdentifiers.PERSISTENT_DRAINS;
	}

	@Override
	public Collection<String> getRequiredSystems() {
		return Arrays.asList( SystemIdentifiers.RETENTION );
	}

	@Override
	public void boot(E env) throws Exception {
            ProfilingPanic.INSTANCE.addAction( new DumpPersistentDrainStatus<EnvironmentWithPersistentDrainManager>( env ) );
		env.setPersistentDrainManager( new PersistentDrainManager<E>(env) );
	}

	@Override
	public void shutdown(E env) {
		env.persistentDrainManager().shutdown();
	}
}
