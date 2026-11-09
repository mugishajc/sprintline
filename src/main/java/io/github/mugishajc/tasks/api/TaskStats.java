package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.TaskStatus;

import java.util.Map;

public record TaskStats(long total, long open, long overdue, Map<TaskStatus, Long> byStatus) {
}
