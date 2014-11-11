package com.goodgame.profiling.commons.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MetricTest {

    @Test
    public void test() {
        Metric m1 = new Metric("server.DOiegzAC.g.MgI7ssgY", 8326803759332840246l, 832.1259329233853);
        Metric m2 = new Metric("server.DOiegzAC.g.MgI7ssgY", 8326803759332840246l, 832.1259329233853);
        
        assertTrue(m1.hashCode() == m2.hashCode());
        assertTrue(m1.equals(m2));
    }

}
