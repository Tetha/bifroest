package com.goodgame.profiling.rewrite_framework.drain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractWrappingDrain;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

/**
 * A value of "U" means "undefined" in Munin. If a drain cannot handle those,
 * this wrapper can filter them out.
 * 
 * @author sglimm
 */
public class FilterUDrain extends AbstractWrappingDrain {
    public FilterUDrain(Drain inner) {
        super(inner);
    }

    @Override
    public void output(List<Metric> metrics) throws IOException {
        List<Metric> filteredMetrics = new ArrayList<>(metrics.size());

        for (Metric metric : metrics) {
            if (!Double.isNaN(metric.value())) {
                filteredMetrics.add(metric);
            }
        }

        inner.output(filteredMetrics);
    }

}
