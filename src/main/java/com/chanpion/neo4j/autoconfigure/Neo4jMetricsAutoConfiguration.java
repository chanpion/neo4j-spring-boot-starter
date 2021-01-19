package com.chanpion.neo4j.autoconfigure;

import com.chanpion.neo4j.actuate.Neo4jDriverMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

/**
 * @author April Chen
 * @date 2021/1/18 20:32
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({MetricsAutoConfiguration.class, Neo4jAutoConfiguration.class})
@ConditionalOnClass({Driver.class, MeterRegistry.class})
public class Neo4jMetricsAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jMetricsAutoConfiguration.class);

    @Autowired
    public void bindDataSourcesToRegistry(Map<String, Driver> drivers, MeterRegistry registry) {
        drivers.forEach((name, driver) -> {
            if (!driver.isMetricsEnabled()) {
                return;
            }
            driver.verifyConnectivityAsync()
                    .thenRunAsync(() -> new Neo4jDriverMetrics(name, driver, Collections.emptyList()).bindTo(registry))
                    .exceptionally(e -> {
                        logger.warn("Could not verify connection for " + driver + " and thus not bind to metrics: " + e
                                .getMessage());
                        return null;
                    });
        });
    }
}
