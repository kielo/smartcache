package org.kielo.smartcache.metrics;

public class MetricsMetadata {
    
    private final String prefix;

    public MetricsMetadata(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
