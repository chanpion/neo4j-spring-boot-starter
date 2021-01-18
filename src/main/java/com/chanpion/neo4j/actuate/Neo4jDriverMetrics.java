package com.chanpion.neo4j.actuate;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.neo4j.driver.ConnectionPoolMetrics;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Metrics;
import org.springframework.util.Assert;

import java.util.function.Consumer;

/**
 * @author April Chen
 * @date 2021/1/18 20:30
 */
public final class Neo4jDriverMetrics implements MeterBinder {
    public static final String PREFIX = "neo4j.driver.connections";
    private static final String BASE_UNIT_CONNECTIONS = "connections";

    private final Driver driver;

    private final Iterable<Tag> tags;

    public Neo4jDriverMetrics(String name, Driver driver, Iterable<Tag> tags) {
        Assert.notNull(name, "Bean name must not be null");
        Assert.notNull(driver, "Driver must not be null");
        Assert.notNull(tags, "Tags must not be null (but may be empty)");
        this.driver = driver;
        this.tags = Tags.concat(tags, "name", name);
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        Metrics metrics = driver.metrics();
        metrics.connectionPoolMetrics().forEach(this.getPoolMetricsBinder(meterRegistry));
    }

    Consumer<ConnectionPoolMetrics> getPoolMetricsBinder(MeterRegistry meterRegistry) {
        return poolMetrics -> {
            Iterable<Tag> poolTags = Tags.concat(tags, "poolId", poolMetrics.id());

            FunctionCounter.builder(PREFIX + ".acquired", poolMetrics, ConnectionPoolMetrics::acquired)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description("The amount of connections that have been acquired.")
                    .register(meterRegistry);

            FunctionCounter.builder(PREFIX + ".closed", poolMetrics, ConnectionPoolMetrics::closed)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description("The amount of connections have been closed.")
                    .register(meterRegistry);

            FunctionCounter.builder(PREFIX + ".created", poolMetrics, ConnectionPoolMetrics::created)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description("The amount of connections have ever been created.")
                    .register(meterRegistry);

            FunctionCounter.builder(PREFIX + ".failedToCreate", poolMetrics, ConnectionPoolMetrics::failedToCreate)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description("The amount of connections have been failed to create.")
                    .register(meterRegistry);

            Gauge.builder(PREFIX + ".idle", poolMetrics, ConnectionPoolMetrics::idle)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description("The amount of connections that are currently idle.")
                    .register(meterRegistry);

            Gauge.builder(PREFIX + ".inUse", poolMetrics, ConnectionPoolMetrics::inUse)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description("The amount of connections that are currently in-use.")
                    .register(meterRegistry);

            FunctionCounter
                    .builder(PREFIX + ".timedOutToAcquire", poolMetrics, ConnectionPoolMetrics::timedOutToAcquire)
                    .tags(poolTags)
                    .baseUnit(BASE_UNIT_CONNECTIONS)
                    .description(
                            "The amount of failures to acquire a connection from a pool within maximum connection acquisition timeout.")
                    .register(meterRegistry);
        };
    }
}
