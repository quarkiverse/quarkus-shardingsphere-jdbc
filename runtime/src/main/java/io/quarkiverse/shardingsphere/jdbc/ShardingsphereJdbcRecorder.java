package io.quarkiverse.shardingsphere.jdbc;

import java.io.IOException;
import java.io.InputStream;

import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.configuration.ConfigurationException;

@Recorder
public class ShardingsphereJdbcRecorder {
    private static final Logger LOG = Logger.getLogger(ShardingsphereJdbcRecorder.class);
    private final ShardingsphereConfig shardingsphere;

    public ShardingsphereJdbcRecorder(ShardingsphereConfig config) {
        this.shardingsphere = config;
    }

    public void evalRules() {
        if (shardingsphere.configFile.isPresent()) {
            String file = shardingsphere.configFile.get();
            try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
                if (input != null) {
                    YamlRootConfiguration rootConfig = YamlEngine.unmarshal(input.readAllBytes(), YamlRootConfiguration.class);
                    rootConfig.getRules().forEach(rule -> {
                        try {
                            if (rule.getRuleConfigurationType().isAssignableFrom(ShardingRuleConfiguration.class)) {
                                ((YamlShardingRuleConfiguration) rule).getTables().forEach((name, table) -> {
                                    final String dataNodes = table.getActualDataNodes();
                                    InlineExpressionParserFactory.newInstance(dataNodes).splitAndEvaluate();
                                });
                                ((YamlShardingRuleConfiguration) rule).getShardingAlgorithms().forEach((name, algorithm) -> {
                                    if (algorithm.getType().equals("INLINE")) {
                                        final String expression = algorithm.getProps().getProperty("algorithm-expression");
                                        InlineExpressionParserFactory.newInstance(expression).evaluateClosure();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            LOG.warn("Eval rules failed with " + e);
                        }
                    });
                }
            } catch (IOException e) {
                throw new ConfigurationException("Can not read " + file);
            }
        }
    }
}
