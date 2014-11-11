package com.goodgame.profiling.commons.systems.configuration;


/**
 * Responsible for loading a JSON configuration from the file system.
 */
public interface JSONConfigurationLoader {
    /**
     * Load the configuration into the environment.
     */
    void loadConfiguration();

    void subscribe(ConfigurationObserver observer);

    void unsubscribe(ConfigurationObserver observer);

    /**
     * Specify a regular expression pattern for searched files. By default, all
     * files ending in ".conf" and are not ending with a "." (i.e. hidden files)
     * are considered.
     *
     * @param pattern
     */
    void setFilePattern(String pattern);

    /**
     * Configure whether comments in JSON are allowed and therefore ignored
     * while parsing.
     *
     * @param doIgnore Whether or not to allow comments. Defaults to
     * <code>true</code>.
     */
    void setIgnoreComments(boolean doIgnore);

    /**
     * Allow comments in JSON files, i.e. ignore all lines starting with
     * <code>ignoredChar</code>.
     *
     * @param ignoredChar The comment character, defaults to <code>#</code>.
     */
    void setIgnoreComments(char ignoredChar);

    /**
     * Configure whether or not search sub-directories recursively.
     *
     * @param doRecurse
     */
    void setRecursive(boolean doRecurse);
}
