package io.github.mugishajc.tasks.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a task. Each status knows which statuses it may move to,
 * which keeps the transition rules in one place.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    BLOCKED,
    DONE,
    CANCELLED;

    public Set<TaskStatus> allowedNext() {
        return switch (this) {
            case TODO -> EnumSet.of(IN_PROGRESS, DONE, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(TODO, BLOCKED, DONE, CANCELLED);
            case BLOCKED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            // finished work can be reopened, cancelled work goes back to the backlog
            case DONE -> EnumSet.of(IN_PROGRESS);
            case CANCELLED -> EnumSet.of(TODO);
        };
    }

    public boolean canMoveTo(TaskStatus next) {
        return next != null && allowedNext().contains(next);
    }

    public boolean isClosed() {
        return this == DONE || this == CANCELLED;
    }
}
