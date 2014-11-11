package com.goodgame.profiling.commons.systems;

/**
 * A collection of common identifiers for subsystems.
 */
public class SystemIdentifiers {

    private SystemIdentifiers() {
        // Utility class - avoid instantiation
    }

    // Common systems

    public static final String LOGGING = "systems.logging";

    public static final String STATISTICS = "systems.statistics";

    public static final String RMIJMX = "systems.rmi-jmx";

    public static final String CONFIGURATION = "systems.configuration";

    public static final String STORAGE = "systems.storage";

    public static final String MULTI_SERVER = "systems.server.multi";

    public static final String CRON = "systems.cron";

    public static final String RETENTION = "systems.retention";

    public static final String PERSISTENT_DRAINS = "persistent-drains";
}
