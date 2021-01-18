package com.chanpion.neo4j.autoconfigure;

import org.neo4j.driver.Config;
import org.neo4j.driver.Logging;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * neo4j配置属性
 *
 * @author April Chen
 * @date 2021/1/18 11:28
 */
@ConfigurationProperties(prefix = "neo4j")
public class Neo4jProperties {

    private URI uri;
    private String username;
    private String password;
    /**
     * Flag, if the driver should use encrypted traffic.
     */
    private boolean encrypted = false;
    private boolean isMetricsEnabled = false;
    private PoolSettings pool;

    public Config asConfig() {
        Config.ConfigBuilder configBuilder = Config.builder();
        configBuilder.withMaxConnectionPoolSize(pool.getMaxConnectionPoolSize())
                .withMaxConnectionLifetime(pool.getMaxConnectionLifetime().toMillis(), TimeUnit.MILLISECONDS)
                .withConnectionAcquisitionTimeout(pool.getConnectionAcquisitionTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .withTrustStrategy(Config.TrustStrategy.trustAllCertificates())
                .withLogging(Logging.slf4j());

        if (encrypted) {
            configBuilder.withEncryption();
        } else {
            configBuilder.withoutEncryption();
        }
        if (isMetricsEnabled) {
            configBuilder.withDriverMetrics();
        } else {
            configBuilder.withoutDriverMetrics();
        }

        return configBuilder.build();
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PoolSettings getPool() {
        return pool;
    }

    public void setPool(PoolSettings pool) {
        this.pool = pool;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isMetricsEnabled() {
        return isMetricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        isMetricsEnabled = metricsEnabled;
    }

    public static class PoolSettings {
        /**
         * 最大连接池连接数量
         */
        private int maxConnectionPoolSize = org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_MAX_CONNECTION_POOL_SIZE;
        /**
         * 最大连接存活时间
         */
        private Duration maxConnectionLifetime = Duration
                .ofMillis(org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_MAX_CONNECTION_LIFETIME);
        /**
         * 获取连接超时时间
         */
        private Duration connectionAcquisitionTimeout = Duration
                .ofMillis(org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_CONNECTION_ACQUISITION_TIMEOUT);

        public int getMaxConnectionPoolSize() {
            return maxConnectionPoolSize;
        }

        public void setMaxConnectionPoolSize(int maxConnectionPoolSize) {
            this.maxConnectionPoolSize = maxConnectionPoolSize;
        }

        public Duration getMaxConnectionLifetime() {
            return maxConnectionLifetime;
        }

        public void setMaxConnectionLifetime(Duration maxConnectionLifetime) {
            this.maxConnectionLifetime = maxConnectionLifetime;
        }

        public Duration getConnectionAcquisitionTimeout() {
            return connectionAcquisitionTimeout;
        }

        public void setConnectionAcquisitionTimeout(Duration connectionAcquisitionTimeout) {
            this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
        }
    }
}
