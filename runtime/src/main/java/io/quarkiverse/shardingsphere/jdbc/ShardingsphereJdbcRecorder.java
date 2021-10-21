package io.quarkiverse.shardingsphere.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.configuration.ConfigurationException;

@Recorder
public class ShardingsphereJdbcRecorder {
    public Supplier<DataSource> shardingsphereDataSourceSupplier(ShardingsphereConfig config, List<String> dataSources) {
        return () -> {
            try {
                Map<String, DataSource> dataSourceMap = dataSources
                        .stream().collect(Collectors.toMap(s -> s, DataSources::fromName));

                return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, getShardingRulesFromConfig(config),
                        new Properties());
            } catch (SQLException e) {
                throw new ConfigurationException(e);
            }
        };
    }

    private List<RuleConfiguration> getShardingRulesFromConfig(ShardingsphereConfig config) {
        List<RuleConfiguration> rules = new ArrayList<>();
        ShardingsphereConfig.ShardingRulesConfig sharding = config.rules.sharding;

        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.setBindingTableGroups(Arrays.asList(sharding.bindingTables.orElse("").split(",")));
        shardingRuleConfiguration.setBroadcastTables(Arrays.asList(sharding.broadcastTables.orElse("").split(",")));

        for (Map.Entry<String, ShardingsphereConfig.ShardingTablesRuleConfig> table : sharding.tables.namedTables.entrySet()) {
            ShardingTableRuleConfiguration tableRule = new ShardingTableRuleConfiguration(table.getKey(),
                    table.getValue().actual_data_nodes);
            ShardingsphereConfig.StandardShardingStrategyConfig shardingStrategyConfig = table
                    .getValue().tableStrategy.standard;
            StandardShardingStrategyConfiguration standard = new StandardShardingStrategyConfiguration(
                    shardingStrategyConfig.column.orElse(
                            sharding.defaultShardingColumn.orElseThrow(
                                    () -> new ConfigurationException("no sharding column for table " + table.getKey()))),
                    shardingStrategyConfig.algorithm);
            tableRule.setTableShardingStrategy(standard);
            tableRule.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration(
                    table.getValue().keyGenerateStrategy.column,
                    table.getValue().keyGenerateStrategy.keyGeneratorName));
            shardingRuleConfiguration.getTables().add(tableRule);
        }

        for (Map.Entry<String, ShardingsphereConfig.ShardingAlgorithmConfig> algorithm : sharding.algorithms.namedAlgorithms
                .entrySet()) {
            Properties properties = new Properties();
            properties.putAll(algorithm.getValue().props);
            ShardingSphereAlgorithmConfiguration shardingSphereAlgorithm = new ShardingSphereAlgorithmConfiguration(
                    algorithm.getValue().type, properties);
            shardingRuleConfiguration.getShardingAlgorithms().put(algorithm.getKey(), shardingSphereAlgorithm);
        }

        for (Map.Entry<String, ShardingsphereConfig.ShardingAlgorithmConfig> keyGenerator : sharding.keyGenerators.namedKeyGenerators
                .entrySet()) {
            Properties properties = new Properties();
            properties.putAll(keyGenerator.getValue().props);
            ShardingSphereAlgorithmConfiguration shardingSphereAlgorithm = new ShardingSphereAlgorithmConfiguration(
                    keyGenerator.getValue().type, properties);
            shardingRuleConfiguration.getKeyGenerators().put(keyGenerator.getKey(), shardingSphereAlgorithm);
        }

        rules.add(shardingRuleConfiguration);
        return rules;
    }
}
