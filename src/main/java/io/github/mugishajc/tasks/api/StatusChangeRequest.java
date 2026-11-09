package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(@NotNull(message = "status is required") TaskStatus status) {
}
