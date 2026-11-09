package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload for creating or replacing a task. Status is not part of it on
 * purpose: status changes go through their own endpoint so the lifecycle
 * rules are always applied.
 */
public record TaskRequest(
        @NotBlank(message = "title is required")
        @Size(max = 120, message = "title must be at most 120 characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        TaskPriority priority,
        LocalDate dueDate) {
}
