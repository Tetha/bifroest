package com.goodgame.profiling.commons.systems.net;

import java.net.Socket;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface IncomingConnectionHandlerFactory<E extends Environment> {
    IncomingConnectionHandler create( Socket socket );
    default void shutdown() {};
}
