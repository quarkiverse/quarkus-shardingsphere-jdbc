package io.quarkiverse.shardingsphere.jdbc.it;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.arc.Arc;

@Path("/shardingsphere-jdbc")
@ApplicationScoped
public class ShardingTablesResource {
    @Inject
    DataSource dataSource;
    @Inject
    EntityManager entityManager;

    @PostConstruct
    void onStart() throws Exception {
        //createAccountTable();
    }

    @PreDestroy
    void onStop() throws Exception {
        //dropAccountTable();
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
    @Transactional
    public void createAccount(Account account) {
        entityManager.persist(account);
    }

    @GET
    @Path("/account/{ds}/{tbl}")
    public Integer count(@PathParam("ds") String ds, @PathParam("tbl") String table) throws Exception {
        DataSource dataSource = Arc.container().instance(DataSource.class, NamedLiteral.of(ds)).get();

        String sql = "SELECT COUNT(*) from " + table;
        try (Connection connection = dataSource.getConnection();
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
