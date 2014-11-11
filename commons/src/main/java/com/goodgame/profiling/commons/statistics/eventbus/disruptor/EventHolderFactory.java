package com.goodgame.profiling.commons.statistics.eventbus.disruptor;

import com.lmax.disruptor.EventFactory;

public class EventHolderFactory implements EventFactory<StatisticEventHolder> {
    @Override
    public StatisticEventHolder newInstance() {
        return new StatisticEventHolder();
    }
}
