package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.Task;
import io.github.mugishajc.tasks.domain.TaskPriority;
import io.github.mugishajc.tasks.domain.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt) {

    public static TaskResponse from(Task task, LocalDate today) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.isOverdue(today),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCompletedAt());
    }
}
