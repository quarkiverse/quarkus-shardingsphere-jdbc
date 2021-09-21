package io.quarkiverse.shardingsphere.jdbc.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ShardingsphereJdbcResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/shardingsphere-jdbc")
                .then()
                .statusCode(200)
                .body(is("Hello shardingsphere-jdbc"));
    }
}
