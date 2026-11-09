package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.TaskPriority;

import java.time.LocalDate;

/**
 * Payload for creating or replacing a task. Status is not part of it on
 * purpose: status changes go through their own endpoint so the lifecycle
 * rules are always applied.
 */
public record TaskRequest(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate) {
}
