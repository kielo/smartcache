package org.kielo.smartcache.metrics;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MetricsMetricsMetadata extends MetricsMetadata {

    private static final String DELIMITER = ".";

    public MetricsMetricsMetadata(String... prefixes) {
        super(
                Arrays.stream(prefixes)
                        .map((p) -> p.replaceAll("\\" + DELIMITER, "_"))
                        .collect(Collectors.joining("."))
        );
    }

    public MetricsMetricsMetadata(String prefix) {
        super(prefix);
    }
}
