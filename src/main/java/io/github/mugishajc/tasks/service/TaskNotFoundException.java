package io.github.mugishajc.tasks.service;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(long id) {
        super("Task " + id + " not found");
    }
}
