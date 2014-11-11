package com.goodgame.profiling.rewrite_framework.source.host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.rewrite_framework.core.source.host.HostFactory;

@MetaInfServices
public class ListHostFactory implements HostFactory {
	@Override
	public String handledType() {
		return "hosts";
	}

	@Override
	public Collection<String> create( JSONObject config ) {
		JSONArray hosts = config.getJSONArray( "hosts" );
		List<String> result = new ArrayList<String>(hosts.length());
		for ( int i = 0; i < hosts.length(); i++ ) {
			result.add( hosts.getString( i ) );
		}
		return Collections.unmodifiableCollection( result );
	}
}
