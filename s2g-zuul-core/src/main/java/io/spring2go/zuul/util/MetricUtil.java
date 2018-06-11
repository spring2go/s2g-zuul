package io.spring2go.zuul.util;

import io.spring2go.metrics.TaggedMetricRegistry;

public class MetricUtil {
	// singleton
	private static TaggedMetricRegistry metricRegistry = new TaggedMetricRegistry();

	public static TaggedMetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

}
