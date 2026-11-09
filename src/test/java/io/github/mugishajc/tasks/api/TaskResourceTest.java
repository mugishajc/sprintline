package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.repository.TaskRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class TaskResourceTest {

    @Inject
    TaskRepository repository;

    @BeforeEach
    void cleanDatabase() {
        QuarkusTransaction.requiringNew().run(repository::deleteAll);
    }

    @Test
    void createsTaskAndReturnsLocation() {
        String due = LocalDate.now().plusDays(2).toString();

        given().contentType(ContentType.JSON)
                .body("""
                        {"title": "  Prepare sprint demo ", "description": "Slides and a short walkthrough",
                         "priority": "HIGH", "dueDate": "%s"}
                        """.formatted(due))
                .when().post("/api/tasks")
                .then()
                .statusCode(201)
                .header("Location", containsString("/api/tasks/"))
                .body("id", notNullValue())
                .body("title", equalTo("Prepare sprint demo"))
                .body("status", equalTo("TODO"))
                .body("priority", equalTo("HIGH"))
                .body("dueDate", equalTo(due))
                .body("overdue", equalTo(false))
                .body("createdAt", notNullValue());
    }

    @Test
    void defaultsPriorityToMedium() {
        given().contentType(ContentType.JSON)
                .body("{\"title\": \"No priority given\"}")
                .when().post("/api/tasks")
                .then()
                .statusCode(201)
                .body("priority", equalTo("MEDIUM"))
                .body("dueDate", nullValue());
    }

    @Test
    void rejectsBlankTitle() {
        given().contentType(ContentType.JSON)
                .body("{\"title\": \"   \"}")
                .when().post("/api/tasks")
                .then()
                .statusCode(400)
                .body(containsString("title is required"));
    }

    @Test
    void rejectsTooLongTitle() {
        given().contentType(ContentType.JSON)
                .body("{\"title\": \"" + "x".repeat(121) + "\"}")
                .when().post("/api/tasks")
                .then()
                .statusCode(400)
                .body(containsString("at most 120"));
    }

    @Test
    void rejectsMissingBody() {
        given().contentType(ContentType.JSON)
                .when().post("/api/tasks")
                .then()
                .statusCode(400);
    }

    @Test
    void readsUpdatesAndDeletesTask() {
        long id = createTask("Draft release notes");

        given().when().get("/api/tasks/{id}", id)
                .then()
                .statusCode(200)
                .body("title", equalTo("Draft release notes"));

        given().contentType(ContentType.JSON)
                .body("{\"title\": \"Publish release notes\", \"priority\": \"URGENT\", \"description\": \"\"}")
                .when().put("/api/tasks/{id}", id)
                .then()
                .statusCode(200)
                .body("title", equalTo("Publish release notes"))
                .body("priority", equalTo("URGENT"))
                .body("description", nullValue());

        given().when().delete("/api/tasks/{id}", id)
                .then().statusCode(204);

        given().when().get("/api/tasks/{id}", id)
                .then().statusCode(404);
    }

    @Test
    void returnsJsonErrorForUnknownTask() {
        given().when().get("/api/tasks/{id}", 999_999)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", equalTo(404))
                .body("message", equalTo("Task 999999 not found"));

        given().when().delete("/api/tasks/{id}", 999_999)
                .then().statusCode(404);
    }

    @Test
    void movesTaskThroughItsLifecycle() {
        long id = createTask("Fix flaky login test");

        changeStatus(id, "IN_PROGRESS")
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("completedAt", nullValue());

        changeStatus(id, "DONE")
                .statusCode(200)
                .body("status", equalTo("DONE"))
                .body("completedAt", notNullValue());

        // reopening clears the completion time
        changeStatus(id, "IN_PROGRESS")
                .statusCode(200)
                .body("completedAt", nullValue());
    }

    @Test
    void rejectsInvalidTransition() {
        long id = createTask("Archive old builds");
        changeStatus(id, "CANCELLED").statusCode(200);

        changeStatus(id, "DONE")
                .statusCode(409)
                .body("message", containsString("Cannot move a task from CANCELLED to DONE"));
    }

    @Test
    void repeatingSameStatusIsHarmless() {
        long id = createTask("Idempotent update");

        changeStatus(id, "TODO")
                .statusCode(200)
                .body("status", equalTo("TODO"));
    }

    @Test
    void statusChangeRequiresStatus() {
        long id = createTask("Missing status");

        given().contentType(ContentType.JSON)
                .body("{}")
                .when().patch("/api/tasks/{id}/status", id)
                .then()
                .statusCode(400)
                .body(containsString("status is required"));
    }

    @Test
    void statsCountTasksByStatus() {
        long first = createTask("One");
        createTask("Two");
        changeStatus(first, "DONE").statusCode(200);

        given().when().get("/api/tasks/stats")
                .then()
                .statusCode(200)
                .body("total", equalTo(2))
                .body("open", equalTo(1))
                .body("byStatus.DONE", equalTo(1))
                .body("byStatus.TODO", equalTo(1))
                .body("byStatus.BLOCKED", equalTo(0));
    }

    private long createTask(String title) {
        return given().contentType(ContentType.JSON)
                .body("{\"title\": \"" + title + "\"}")
                .when().post("/api/tasks")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private io.restassured.response.ValidatableResponse changeStatus(long id, String status) {
        return given().contentType(ContentType.JSON)
                .body("{\"status\": \"" + status + "\"}")
                .when().patch("/api/tasks/{id}/status", id)
                .then();
    }
}
