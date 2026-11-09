package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.Task;
import io.github.mugishajc.tasks.domain.TaskPriority;
import io.github.mugishajc.tasks.domain.TaskStatus;
import io.github.mugishajc.tasks.repository.TaskRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class TaskListingTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Inject
    TaskRepository repository;

    @BeforeEach
    void seed() {
        QuarkusTransaction.requiringNew().run(() -> {
            repository.deleteAll();
            persist("Pay invoices", "Supplier invoices for October", TaskStatus.TODO, TaskPriority.HIGH, TODAY.minusDays(2));
            persist("Book team offsite", null, TaskStatus.IN_PROGRESS, TaskPriority.LOW, TODAY.plusDays(20));
            persist("Rotate API keys", "Quarterly key rotation", TaskStatus.TODO, TaskPriority.URGENT, TODAY.plusDays(1));
            persist("Update invoice template", null, TaskStatus.DONE, TaskPriority.MEDIUM, TODAY.minusDays(5));
            persist("Plan Q1 roadmap", null, TaskStatus.BLOCKED, TaskPriority.MEDIUM, null);
        });
    }

    @Test
    void listsEverythingWithPagingMetadata() {
        given().when().get("/api/tasks")
                .then()
                .statusCode(200)
                .body("items", hasSize(5))
                .body("page", equalTo(0))
                .body("size", equalTo(20))
                .body("totalItems", equalTo(5))
                .body("totalPages", equalTo(1));
    }

    @Test
    void pagesThroughResults() {
        given().queryParam("size", 2).queryParam("page", 2).queryParam("sort", "title")
                .when().get("/api/tasks")
                .then()
                .statusCode(200)
                .body("items", hasSize(1))
                .body("items[0].title", equalTo("Update invoice template"))
                .body("totalItems", equalTo(5))
                .body("totalPages", equalTo(3));
    }

    @Test
    void filtersByStatusCaseInsensitively() {
        given().queryParam("status", "todo")
                .when().get("/api/tasks")
                .then()
                .statusCode(200)
                .body("items.title", containsInAnyOrder("Pay invoices", "Rotate API keys"));
    }

    @Test
    void filtersByPriority() {
        given().queryParam("priority", "MEDIUM")
                .when().get("/api/tasks")
                .then()
                .body("items.title", containsInAnyOrder("Update invoice template", "Plan Q1 roadmap"));
    }

    @Test
    void searchesTitleAndDescription() {
        given().queryParam("q", "INVOICE")
                .when().get("/api/tasks")
                .then()
                .body("items.title", containsInAnyOrder("Pay invoices", "Update invoice template"));

        given().queryParam("q", "rotation")
                .when().get("/api/tasks")
                .then()
                .body("items.title", contains("Rotate API keys"));
    }

    @Test
    void filtersByDueDateRange() {
        given().queryParam("dueAfter", TODAY.toString())
                .queryParam("dueBefore", TODAY.plusDays(7).toString())
                .when().get("/api/tasks")
                .then()
                .body("items.title", contains("Rotate API keys"));
    }

    @Test
    void findsOverdueOpenTasksOnly() {
        // the DONE task is also past due but should not count as overdue
        given().queryParam("overdue", true)
                .when().get("/api/tasks")
                .then()
                .body("items.title", contains("Pay invoices"))
                .body("items[0].overdue", equalTo(true));

        given().queryParam("overdue", false)
                .when().get("/api/tasks")
                .then()
                .body("items", hasSize(4));
    }

    @Test
    void combinesFilters() {
        given().queryParam("status", "TODO").queryParam("priority", "URGENT")
                .when().get("/api/tasks")
                .then()
                .body("items.title", contains("Rotate API keys"));
    }

    @Test
    void sortsByPriorityInBusinessOrder() {
        given().queryParam("sort", "-priority,title")
                .when().get("/api/tasks")
                .then()
                .body("items.priority", contains("URGENT", "HIGH", "MEDIUM", "MEDIUM", "LOW"))
                .body("items[2].title", equalTo("Plan Q1 roadmap"));
    }

    @Test
    void rejectsUnknownSortField() {
        given().queryParam("sort", "password")
                .when().get("/api/tasks")
                .then()
                .statusCode(400)
                .body("message", containsString("Cannot sort by 'password'"));
    }

    @Test
    void rejectsBadFilterValues() {
        given().queryParam("status", "SOMEDAY").when().get("/api/tasks")
                .then().statusCode(400).body("message", containsString("Invalid status"));

        given().queryParam("dueBefore", "next week").when().get("/api/tasks")
                .then().statusCode(400).body("message", containsString("expected yyyy-MM-dd"));
    }

    @Test
    void rejectsOutOfRangePaging() {
        given().queryParam("size", 500).when().get("/api/tasks").then().statusCode(400);
        given().queryParam("page", -1).when().get("/api/tasks").then().statusCode(400);
    }

    @Test
    void statsReportOverdueCount() {
        given().when().get("/api/tasks/stats")
                .then()
                .statusCode(200)
                .body("total", equalTo(5))
                .body("open", equalTo(4))
                .body("overdue", equalTo(1));
    }

    private void persist(String title, String description, TaskStatus status, TaskPriority priority, LocalDate due) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(due);
        repository.persist(task);
    }
}
