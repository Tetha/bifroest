package com.goodgame.profiling.rewrite_framework.core.output.generator;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.MultipleWrappingFactory;

public interface MultipleWrappingNameGeneratorFactory<E extends Environment> extends MultipleWrappingFactory<E, MetricNameGenerator> {
}
