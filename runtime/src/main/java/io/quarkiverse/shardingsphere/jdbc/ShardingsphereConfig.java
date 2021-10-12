package io.quarkiverse.shardingsphere.jdbc;

import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.infra.database.DefaultSchema;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "shardingsphere", phase = ConfigPhase.RUN_TIME)
public class ShardingsphereConfig {

    /**
     * Schema Name of dataSource
     */
    @ConfigItem(name = "schema.name", defaultValue = DefaultSchema.LOGIC_NAME)
    public String schemaName;

    /**
     * rules
     */
    @ConfigItem
    public RulesConfig rules;

    @ConfigGroup
    public static class RulesConfig {
        /**
         * sharding rules
         */
        @ConfigItem
        public ShardingRulesConfig sharding;
    }

    @ConfigGroup
    public static class ShardingRulesConfig {

        /**
         * default sharding column
         */
        @ConfigItem(name = "default-sharding-column")
        public Optional<String> defaultShardingColumn;

        /**
         * binding tables
         */
        @ConfigItem(name = "binding-tables")
        public Optional<String> bindingTables;

        /**
         * broadcast tables
         */
        @ConfigItem(name = "broadcast-tables")
        public Optional<String> broadcastTables;

        /**
         * tables
         */
        @ConfigItem(name = "tables")
        public TablesConfig tables;

        /**
         * sharding algorithms
         */
        @ConfigItem(name = "sharding-algorithms")
        public ShardingAlgorithmsConfig algorithms;

        /**
         * key generators
         */
        @ConfigItem(name = "key-generators")
        public KeyGeneratorsConfig keyGenerators;
    }

    @ConfigGroup
    public static class TablesConfig {
        /**
         * Sharding table rules configuration
         */
        @ConfigDocSection
        @ConfigDocMapKey("sharding-table-rules")
        @ConfigItem(name = ConfigItem.PARENT)
        public Map<String, ShardingTablesRuleConfig> namedTables;
    }

    @ConfigGroup
    public static class ShardingTablesRuleConfig {
        /**
         * Actual data nodes
         */
        @ConfigItem(name = "actual-data-nodes")
        public String actual_data_nodes;

        /**
         * Table sharding strategy
         */
        @ConfigItem(name = "table-strategy")
        public TableStrategyConfig tableStrategy;

        /**
         * Key generator strategy
         */
        @ConfigItem(name = "key-generate-strategy")
        public KeyGenerateStrategyConfig keyGenerateStrategy;
    }

    @ConfigGroup
    public static class TableStrategyConfig {
        /**
         * Standard strategy
         */
        @ConfigItem
        public StandardShardingStrategyConfig standard;
    }

    @ConfigGroup
    public static class StandardShardingStrategyConfig {
        /**
         * Sharding column
         */
        @ConfigItem(name = "sharding-column")
        public Optional<String> column;

        /**
         * Sharding algorithm name
         */
        @ConfigItem(name = "sharding-algorithm-name")
        public String algorithm;
    }

    @ConfigGroup
    public static class KeyGenerateStrategyConfig {
        /**
         * column
         */
        @ConfigItem
        public String column;

        /**
         * generator name
         */
        @ConfigItem(name = "key-generator-name")
        public String keyGeneratorName;
    }

    @ConfigGroup
    public static class ShardingAlgorithmsConfig {
        /**
         * Sharding algorithms configuration
         */
        @ConfigDocSection
        @ConfigDocMapKey("sharding-algorithms")
        @ConfigItem(name = ConfigItem.PARENT)
        public Map<String, ShardingAlgorithmConfig> namedAlgorithms;
    }

    @ConfigGroup
    public static class KeyGeneratorsConfig {
        /**
         * Key generators configuration
         */
        @ConfigDocSection
        @ConfigDocMapKey("key-generators")
        @ConfigItem(name = ConfigItem.PARENT)
        public Map<String, ShardingAlgorithmConfig> namedKeyGenerators;
    }

    @ConfigGroup
    public static class ShardingAlgorithmConfig {
        /**
         * algorithm type
         */
        @ConfigItem
        public String type;

        /**
         * algorithm properties
         */
        @ConfigItem
        public Map<String, String> props;
    }
}
