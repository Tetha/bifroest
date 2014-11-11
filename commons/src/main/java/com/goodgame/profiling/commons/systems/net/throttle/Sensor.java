package com.goodgame.profiling.commons.systems.net.throttle;

public interface Sensor {
    /* Returns a value x with 0 <= x <= 1
     * 0 being no load at all
     * 1 being maximum load. */
    double getValue();
}
