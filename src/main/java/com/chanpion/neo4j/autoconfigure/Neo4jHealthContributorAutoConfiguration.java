package com.chanpion.neo4j.autoconfigure;

import com.chanpion.neo4j.actuate.Neo4jHealthIndicator;
import org.neo4j.driver.Driver;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author April Chen
 * @date 2021/1/19 11:28
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Driver.class)
@ConditionalOnBean(Driver.class)
@ConditionalOnEnabledHealthIndicator("neo4j")
@AutoConfigureBefore(HealthContributorAutoConfiguration.class)
@AutoConfigureAfter(Neo4jAutoConfiguration.class)
public class Neo4jHealthContributorAutoConfiguration
        extends CompositeHealthContributorConfiguration<Neo4jHealthIndicator, Driver> {

    @Bean
    @ConditionalOnMissingBean(name = "neo4jHealthContributor")
    public HealthContributor neo4jHealthContributor(Map<String, Driver> drivers) {
        return createContributor(drivers);
    }
}
