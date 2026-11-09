package io.github.mugishajc.tasks.service;

import io.github.mugishajc.tasks.domain.TaskStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TaskStatus from, TaskStatus to) {
        super("Cannot move a task from " + from + " to " + to + ". Allowed: " + from.allowedNext());
    }
}
