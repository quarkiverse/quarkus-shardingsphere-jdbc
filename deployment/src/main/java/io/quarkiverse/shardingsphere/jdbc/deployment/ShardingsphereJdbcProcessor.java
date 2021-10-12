package io.quarkiverse.shardingsphere.jdbc.deployment;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.jboss.jandex.DotName;

import io.quarkiverse.shardingsphere.jdbc.ShardingsphereConfig;
import io.quarkiverse.shardingsphere.jdbc.ShardingsphereJdbcRecorder;
import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ShardingsphereJdbcProcessor {

    private static final String FEATURE = "shardingsphere-jdbc";
    private static final DotName DATA_SOURCE = DotName.createSimple(javax.sql.DataSource.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateShardingSphereDataSource(ShardingsphereJdbcRecorder recorder,
            List<JdbcDataSourceBuildItem> dataSourceBuildItems,
            ShardingsphereConfig config,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanBuildItemBuildProducer) {
        List<String> dataSources = dataSourceBuildItems
                .stream().filter(ds -> !ds.isDefault()).map(ds -> ds.getName()).collect(Collectors.toList());

        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(ShardingSphereDataSource.class)
                .addType(DATA_SOURCE)
                .scope(Singleton.class)
                .setRuntimeInit()
                .unremovable()
                .supplier(recorder.shardingsphereDataSourceSupplier(config, dataSources));

        syntheticBeanBuildItemBuildProducer.produce(configurator.done());
    }
}
