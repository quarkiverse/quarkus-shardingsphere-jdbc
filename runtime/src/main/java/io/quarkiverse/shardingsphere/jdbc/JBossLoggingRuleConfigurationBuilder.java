package io.quarkiverse.shardingsphere.jdbc;

import java.util.Collections;

import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.constant.LoggingOrder;
import org.apache.shardingsphere.logging.rule.builder.LoggingRuleBuilder;

public class JBossLoggingRuleConfigurationBuilder
        implements DefaultGlobalRuleConfigurationBuilder<LoggingRuleConfiguration, LoggingRuleBuilder> {
    @Override
    public LoggingRuleConfiguration build() {
        return new LoggingRuleConfiguration(Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public int getOrder() {
        return LoggingOrder.ORDER;
    }

    @Override
    public Class<LoggingRuleBuilder> getTypeClass() {
        return LoggingRuleBuilder.class;
    }
}
