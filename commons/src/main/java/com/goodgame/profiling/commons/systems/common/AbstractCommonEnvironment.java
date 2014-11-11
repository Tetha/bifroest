package com.goodgame.profiling.commons.systems.common;

import java.nio.file.Path;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.EnvironmentWithInit;
import com.goodgame.profiling.commons.boot.InitD;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithMutableJSONConfiguration;
import com.goodgame.profiling.commons.systems.configuration.JSONConfigurationLoader;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithMutableStatisticsGatherer;

public abstract class AbstractCommonEnvironment implements EnvironmentWithConfigPath, EnvironmentWithMutableJSONConfiguration,
        EnvironmentWithMutableStatisticsGatherer, EnvironmentWithInit {

    private final Path configPath;
    private final InitD init;
    private JSONObject configuration;
    private JSONConfigurationLoader configLoader;
    private StatisticGatherer statisticGatherer;

    public AbstractCommonEnvironment( Path configPath, InitD init ) {
        this.configPath = configPath;
        this.init = init;
    }

    @Override
    public Path getConfigPath() {
        return configPath;
    }

    @Override
    public InitD initD() {
        return init;
    }

    @Override
    public JSONObject getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration( JSONObject object ) {
        this.configuration = object;
    }

    @Override
    public JSONConfigurationLoader getConfigurationLoader() {
        return configLoader;
    }

    @Override
    public void setConfigurationLoader( JSONConfigurationLoader loader ) {
        this.configLoader = loader;
    }

    @Override
    public StatisticGatherer statisticGatherer() {
        return statisticGatherer;
    }

    @Override
    public void setStatisticGatherer( StatisticGatherer statisticGatherer ) {
        this.statisticGatherer = statisticGatherer;
    }
}
