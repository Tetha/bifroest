package com.goodgame.profiling.commons.systems.net;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;

public interface IncomingConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> {
    String handledFormat();
    IncomingConnectionHandlerFactory<E> createFactory( E environment, String interfaceName );
}
