package io.github.mugishajc.tasks.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskStatusTest {

    @Test
    void openWorkCanBeFinishedOrCancelled() {
        assertTrue(TaskStatus.TODO.canMoveTo(TaskStatus.DONE));
        assertTrue(TaskStatus.IN_PROGRESS.canMoveTo(TaskStatus.DONE));
        assertTrue(TaskStatus.BLOCKED.canMoveTo(TaskStatus.CANCELLED));
    }

    @Test
    void blockedWorkMustBeResumedBeforeItIsDone() {
        assertFalse(TaskStatus.BLOCKED.canMoveTo(TaskStatus.DONE));
        assertTrue(TaskStatus.BLOCKED.canMoveTo(TaskStatus.IN_PROGRESS));
    }

    @Test
    void closedTasksOnlyReopen() {
        assertTrue(TaskStatus.DONE.canMoveTo(TaskStatus.IN_PROGRESS));
        assertFalse(TaskStatus.DONE.canMoveTo(TaskStatus.CANCELLED));
        assertTrue(TaskStatus.CANCELLED.canMoveTo(TaskStatus.TODO));
        assertFalse(TaskStatus.CANCELLED.canMoveTo(TaskStatus.DONE));
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    void noStatusTransitionsToItselfOrNull(TaskStatus status) {
        assertFalse(status.canMoveTo(status));
        assertFalse(status.canMoveTo(null));
    }

    @Test
    void overdueIgnoresClosedTasksAndMissingDates() {
        LocalDate today = LocalDate.of(2026, 3, 10);
        Task task = new Task();
        task.setTitle("t");

        assertFalse(task.isOverdue(today));

        task.setDueDate(today.minusDays(1));
        assertTrue(task.isOverdue(today));

        task.setDueDate(today);
        assertFalse(task.isOverdue(today), "due today is not overdue yet");

        task.setDueDate(today.minusDays(1));
        task.setStatus(TaskStatus.DONE);
        assertFalse(task.isOverdue(today));
    }
}
