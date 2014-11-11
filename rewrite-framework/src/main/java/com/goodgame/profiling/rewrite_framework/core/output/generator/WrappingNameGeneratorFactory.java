package com.goodgame.profiling.rewrite_framework.core.output.generator;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.WrappingFactory;

public interface WrappingNameGeneratorFactory<E extends Environment> extends WrappingFactory<E, MetricNameGenerator> {
}
