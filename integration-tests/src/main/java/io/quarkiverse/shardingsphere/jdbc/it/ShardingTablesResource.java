package io.quarkiverse.shardingsphere.jdbc.it;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;

@Path("/shardingsphere-jdbc")
@ApplicationScoped
public class ShardingTablesResource {
    @Inject
    DataSource dataSource;

    @Inject
    EntityManager entityManager;

    private static final Logger LOG = Logger.getLogger(ShardingTablesResource.class);

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
        LOG.debug("create " + account);
        entityManager.persist(account);
    }

    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAccount() {
        return entityManager.createNamedQuery("findAll", Account.class).getResultList();
    }

    @GET
    @Path("/account/{ds}/{tbl}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account count(@PathParam("ds") String ds, @PathParam("tbl") String table) throws Exception {
        DataSource dataSource = Arc.container().instance(DataSource.class, NamedLiteral.of(ds)).get();
        Account account = new Account();

        String sql = "SELECT user_id, account_id, status from " + table;
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(sql);
            if (result.next()) {
                account.setUser_id(result.getInt(1));
                account.setAccount_id(result.getInt(2));
                account.setStatus(result.getString(3));
            }
        }
        return account;
    }
}
