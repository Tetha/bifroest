package com.goodgame.profiling.stream_rewriter;

import java.util.Map;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.IncomingConnectionHandlerFactoryFactory;
import com.goodgame.profiling.commons.systems.net.multiserver.MultiServerSystem;

@MetaInfServices
public class MetricConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> implements IncomingConnectionHandlerFactoryFactory<E> {
    private Map<String, JSONObject> interfaceConfigs; // quasi-final

    @Override
    public String handledFormat() {
        return "plain-text-metrics";
    }

    @Override
    public MetricConnectionHandlerFactory<E> createFactory( E environment, String interfaceName ) {
        synchronized ( this ) {
            if ( interfaceConfigs == null ) {
                this.interfaceConfigs = MultiServerSystem.createInterfaceConfigs( environment );
            }
        }

        return new MetricConnectionHandlerFactory<E>( environment, interfaceName, interfaceConfigs.get( interfaceName ) );
    }
}
