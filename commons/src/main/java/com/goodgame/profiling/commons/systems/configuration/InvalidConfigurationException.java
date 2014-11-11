package com.goodgame.profiling.commons.systems.configuration;

public class InvalidConfigurationException extends Exception {

    private static final long serialVersionUID = -1838816716088428245L;

    public InvalidConfigurationException() {
        super();
    }

    public InvalidConfigurationException( String message ) {
        super( message );
    }

    public InvalidConfigurationException( Throwable cause ) {
        super( cause );
    }

    public InvalidConfigurationException( String message, Throwable cause ) {
        super( message, cause );
    }

}
