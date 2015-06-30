package org.kielo.smartcache.metrics;

@FunctionalInterface
public interface MetricNameSupplier {

    String nameFor(MeteredEventType event, String region, String key);

}
