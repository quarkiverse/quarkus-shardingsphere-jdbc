package io.quarkiverse.shardingsphere.jdbc.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ShardingTablesTest {

    @Test
    public void test() {
        List<Account> accounts = List.of(
                new Account(0, 0, "true"),
                new Account(1, 1, "false"),
                new Account(2, 1, "true"),
                new Account(3, 0, "false"));
        accounts.stream().forEach(
                account -> create(account.getAccount_id(), account.getUser_id(), Boolean.parseBoolean(account.getStatus())));

        given().get("/shardingsphere-jdbc/account/ds_0/t_account_0")
                .then()
                .statusCode(200)
                .body("account_id", is(0));

        given().get("/shardingsphere-jdbc/account/ds_0/t_account_1")
                .then()
                .statusCode(200)
                .body("account_id", is(3));

        given().get("/shardingsphere-jdbc/account/ds_1/t_account_0")
                .then()
                .statusCode(200)
                .body("account_id", is(2));

        given().get("/shardingsphere-jdbc/account/ds_1/t_account_1")
                .then()
                .statusCode(200)
                .body("account_id", is(1));

        given().get("/shardingsphere-jdbc/account")
                .then()
                .statusCode(200)
                .body("size()", is(4));
        //.body("$", Matchers.hasItems(accounts));
    }

    private void create(int account_id, int user_id, boolean status) {
        given()
                .body("{\"account_id\":" + account_id + ", \"user_id\":" + user_id + ", \"status\":\"" + status + "\"}")
                .contentType(ContentType.JSON)
                .post("/shardingsphere-jdbc/account")
                .then()
                .statusCode(204);
    }
}
