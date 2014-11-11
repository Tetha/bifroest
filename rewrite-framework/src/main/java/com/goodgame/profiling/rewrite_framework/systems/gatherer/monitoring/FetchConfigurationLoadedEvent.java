package com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring;

import com.goodgame.profiling.rewrite_framework.core.config.FetchConfiguration;

@SuppressWarnings( "rawtypes" )
public final class FetchConfigurationLoadedEvent {
    private final FetchConfiguration fetchConf;

    public FetchConfigurationLoadedEvent( FetchConfiguration fetchConf ) {
        this.fetchConf = fetchConf;
    }

    public FetchConfiguration fetchConf() {
        return fetchConf;
    }
}
