package io.quarkiverse.shardingsphere.jdbc.it;

import io.quarkus.test.junit.QuarkusTestProfile;

public class ShardingTablesProfile implements QuarkusTestProfile {
    @Override
    public String getConfigProfile() {
        return "sharding-tables";
    }
}
