package io.quarkiverse.shardingsphere.jdbc;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "shardingsphere", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class ShardingsphereConfig {
    /**
     * config file
     */
    @ConfigItem
    public Optional<String> configFile;
}
