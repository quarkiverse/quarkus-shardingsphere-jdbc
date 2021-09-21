package io.quarkiverse.shardingsphere.jdbc.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ShardingsphereJdbcProcessor {

    private static final String FEATURE = "shardingsphere-jdbc";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
