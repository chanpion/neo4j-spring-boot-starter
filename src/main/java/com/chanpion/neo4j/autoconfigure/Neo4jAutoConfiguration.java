package com.chanpion.neo4j.autoconfigure;

import org.neo4j.driver.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j自动配置
 *
 * @author April Chen
 * @date 2021/1/18 13:34
 */
@Configuration
@ConditionalOnClass(Driver.class)
@EnableConfigurationProperties(Neo4jProperties.class)
public class Neo4jAutoConfiguration {
    private final Neo4jProperties neo4jProperties;

    public Neo4jAutoConfiguration(Neo4jProperties neo4jProperties) {
        this.neo4jProperties = neo4jProperties;
    }

    @Bean
    @ConditionalOnMissingBean(Driver.class)
    public Driver driver() {
        final AuthToken authToken = AuthTokens.basic(neo4jProperties.getUsername(), neo4jProperties.getPassword());
        final Config config = neo4jProperties.asConfig();
        return GraphDatabase.driver(neo4jProperties.getUri(), authToken, config);
    }
}
