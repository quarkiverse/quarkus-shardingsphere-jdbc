package io.quarkiverse.shardingsphere.jdbc.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ShardingsphereJdbcResourceTest {

    @Test
    public void test() {
        given()
                .body("{\"account_id\":1, \"user_id\":1, \"status\":\"true\"}").contentType(ContentType.JSON)
                .post("/shardingsphere-jdbc/account")
                .then()
                .statusCode(200)
                .body(is("1"));

        given().get("/shardingsphere-jdbc/account/t_account_0")
                .then()
                .statusCode(200)
                .body(is("0"));

        given().get("/shardingsphere-jdbc/account/t_account_1")
                .then()
                .statusCode(200)
                .body(is("1"));
    }
}
