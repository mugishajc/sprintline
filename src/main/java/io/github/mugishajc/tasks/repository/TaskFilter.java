package io.github.mugishajc.tasks.repository;

import io.github.mugishajc.tasks.domain.TaskPriority;
import io.github.mugishajc.tasks.domain.TaskStatus;

import java.time.LocalDate;

/**
 * Optional criteria for listing tasks. Null fields are ignored.
 */
public record TaskFilter(
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueBefore,
        LocalDate dueAfter,
        String text,
        Boolean overdue) {

    public static TaskFilter none() {
        return new TaskFilter(null, null, null, null, null, null);
    }
}
