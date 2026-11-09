package io.github.mugishajc.tasks.health;

import io.github.mugishajc.tasks.repository.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Ready once the tasks table can actually be queried. The datasource check
 * that Quarkus adds only proves a connection can be opened.
 */
@Readiness
@ApplicationScoped
public class TaskStoreHealthCheck implements HealthCheck {

    private final TaskRepository repository;

    public TaskStoreHealthCheck(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public HealthCheckResponse call() {
        try {
            long count = repository.count();
            return HealthCheckResponse.named("task-store").up().withData("tasks", count).build();
        } catch (RuntimeException e) {
            return HealthCheckResponse.named("task-store").down().withData("error", e.getMessage()).build();
        }
    }
}
