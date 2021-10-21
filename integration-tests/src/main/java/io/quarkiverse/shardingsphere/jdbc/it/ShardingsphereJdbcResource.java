package io.quarkiverse.shardingsphere.jdbc.it;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@Path("/shardingsphere-jdbc")
@ApplicationScoped
public class ShardingsphereJdbcResource {
    @Inject
    DataSource dataSource;

    @Named("ds")
    DataSource h2_ds;

    void onStart(@Observes StartupEvent ev) throws Exception {
        createAccountTable();
    }

    void onStop(@Observes ShutdownEvent ev) throws Exception {
        dropAccountTable();
    }

    private void createAccountTable() throws SQLException {
        String sql = "CREATE TABLE t_account (account_id BIGINT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (account_id))";
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void dropAccountTable() throws SQLException {
        String sql = "DROP TABLE t_account";
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @POST
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Integer createAccount(Account account) throws Exception {
        int result = 0;

        String sql = "INSERT INTO t_account (account_id, user_id, status) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, account.getAccount_id());
            statement.setInt(2, account.getUser_id());
            statement.setString(3, account.getStatus());
            result = statement.executeUpdate();
        }

        return result;
    }

    @GET
    @Path("/account/{tbl}")
    public Integer count(@PathParam("tbl") String table) throws Exception {
        String sql = "SELECT COUNT(*) from " + table;
        try (Connection connection = h2_ds.getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(sql);
            if (result.next()) {
                return result.getInt(1);
            } else {
                return -1;
            }
        }
    }
}