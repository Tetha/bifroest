package com.goodgame.profiling.rewrite_framework.source.host;

import java.util.Collection;
import java.util.Collections;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.rewrite_framework.core.source.host.HostFactory;

@MetaInfServices
public class SingleHostFactory implements HostFactory {
	@Override
	public String handledType() {
		return "host";
	}

	@Override
	public Collection<String> create( JSONObject config ) {
		return Collections.singleton( config.getString( "host" ) );
	}
}
