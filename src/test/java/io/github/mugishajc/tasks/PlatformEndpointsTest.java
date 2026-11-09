package io.github.mugishajc.tasks;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
class PlatformEndpointsTest {

    @Test
    void readinessIncludesTaskStore() {
        given().when().get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("checks.name", hasItem("task-store"));
    }

    @Test
    void livenessIsUp() {
        given().when().get("/q/health/live")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void publishesOpenApiDocument() {
        given().queryParam("format", "json")
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("/api/tasks/{id}/status"))
                .body(containsString("Task Manager API"));
    }
}
