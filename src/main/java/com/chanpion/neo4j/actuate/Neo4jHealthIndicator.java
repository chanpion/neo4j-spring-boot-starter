package com.chanpion.neo4j.actuate;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.summary.DatabaseInfo;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.util.StringUtils;

/**
 * @author April Chen
 * @date 2021/1/18 20:25
 */
public final class Neo4jHealthIndicator extends AbstractHealthIndicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jHealthIndicator.class);

    /**
     * The Cypher statement used to verify Neo4j is up.
     */
    static final String CYPHER = "CALL dbms.components() YIELD name, edition WHERE name = 'Neo4j Kernel' RETURN edition";
    /**
     * Message indicating that the health check failed.
     */
    static final String MESSAGE_HEALTH_CHECK_FAILED = "Neo4j health check failed";
    /**
     * Message logged before retrying a health check.
     */
    static final String MESSAGE_SESSION_EXPIRED = "Neo4j session has expired, retrying one single time to retrieve server health.";
    /**
     * The default session config to use while connecting.
     */
    static final SessionConfig DEFAULT_SESSION_CONFIG = SessionConfig.builder().withDefaultAccessMode(AccessMode.WRITE)
            .build();

    private final Driver driver;

    public Neo4jHealthIndicator(Driver driver) {
        super(MESSAGE_HEALTH_CHECK_FAILED);
        this.driver = driver;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            ResultSummaryWithEdition resultSummaryWithEdition;
            // Retry one time when the session has been expired
            try {
                resultSummaryWithEdition = runHealthCheckQuery();
            } catch (SessionExpiredException sessionExpiredException) {
                LOGGER.warn(MESSAGE_SESSION_EXPIRED);
                resultSummaryWithEdition = runHealthCheckQuery();
            }
            buildStatusUp(resultSummaryWithEdition, builder);
        } catch (Exception ex) {
            builder.down().withException(ex);
        }
    }

    static void buildStatusUp(ResultSummaryWithEdition resultSummaryWithEdition, Health.Builder builder) {
        ServerInfo serverInfo = resultSummaryWithEdition.resultSummary.server();
        DatabaseInfo databaseInfo = resultSummaryWithEdition.resultSummary.database();

        builder.up()
                .withDetail("server", serverInfo.version() + "@" + serverInfo.address())
                .withDetail("edition", resultSummaryWithEdition.edition);

        if (StringUtils.hasText(databaseInfo.name())) {
            builder.withDetail("database", databaseInfo.name());
        }
    }

    ResultSummaryWithEdition runHealthCheckQuery() {

        try (Session session = this.driver.session(DEFAULT_SESSION_CONFIG)) {

            // We use WRITE here to make sure UP is returned for a server that supports
            // all possible workloads
            return session.writeTransaction(tx -> {
                Result result = tx.run(CYPHER);
                String edition = result.single().get("edition").asString();
                ResultSummary resultSummary = result.consume();
                return new ResultSummaryWithEdition(resultSummary, edition);
            });
        }
    }
}
