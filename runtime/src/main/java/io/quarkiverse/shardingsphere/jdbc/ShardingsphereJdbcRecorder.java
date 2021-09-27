package io.quarkiverse.shardingsphere.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ShardingsphereJdbcRecorder {
    public Supplier<DataSource> shardingsphereDataSourceSupplier(List<String> dataSources) {
        return () -> {
            try {
                Map<String, DataSource> dataSourceMap = dataSources
                        .stream().collect(Collectors.toMap(s -> s, DataSources::fromName));

                return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, null, new Properties());
            } catch (SQLException e) {
                return null;
            }
        };
    }
}
