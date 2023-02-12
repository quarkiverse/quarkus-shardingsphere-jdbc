package io.quarkiverse.shardingsphere.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import jakarta.enterprise.inject.literal.NamedLiteral;

import org.eclipse.microprofile.config.ConfigProvider;

import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.Arc;

public class QuarkusDataSource implements DataSource {
    private String dsName;
    private AgroalDataSource dataSource;

    public String getDsName() {
        return dsName;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public String getUrl() {
        return ConfigProvider.getConfig().getValue("quarkus.datasource." + dsName + ".jdbc.url", String.class);
    }

    public void setUrl(String url) {
    }

    public String getUsername() {
        return ConfigProvider.getConfig().getValue("quarkus.datasource." + dsName + ".username", String.class);
    }

    public void setUsername(String username) {
    }

    private AgroalDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = Arc.container().instance(AgroalDataSource.class, NamedLiteral.of(dsName)).get();
        }
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String s, String s1) throws SQLException {
        return getDataSource().getConnection(s, s1);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {
        getDataSource().setLogWriter(printWriter);
    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {
        getDataSource().setLoginTimeout(i);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return getDataSource().unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return getDataSource().isWrapperFor(aClass);
    }
}
