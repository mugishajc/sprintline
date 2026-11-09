package io.github.mugishajc.tasks.service;

import io.github.mugishajc.tasks.api.PageResponse;
import io.github.mugishajc.tasks.api.TaskRequest;
import io.github.mugishajc.tasks.api.TaskResponse;
import io.github.mugishajc.tasks.domain.Task;
import io.github.mugishajc.tasks.domain.TaskPriority;
import io.github.mugishajc.tasks.domain.TaskStatus;
import io.github.mugishajc.tasks.repository.TaskFilter;
import io.github.mugishajc.tasks.repository.TaskRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TaskService {

    private final TaskRepository repository;
    private final Clock clock;

    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public PageResponse<TaskResponse> list(TaskFilter filter, int page, int size, String sort) {
        LocalDate today = today();
        PanacheQuery<Task> query = repository.search(filter, today, TaskSort.parse(sort))
                .page(Page.of(page, size));
        List<TaskResponse> items = query.list().stream()
                .map(task -> TaskResponse.from(task, today))
                .toList();
        return new PageResponse<>(items, page, size, query.count(), query.pageCount());
    }

    public TaskResponse get(long id) {
        return TaskResponse.from(load(id), today());
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        apply(task, request);
        repository.persist(task);
        return TaskResponse.from(task, today());
    }

    @Transactional
    public TaskResponse update(long id, TaskRequest request) {
        Task task = load(id);
        apply(task, request);
        // flush so the @PreUpdate timestamp is reflected in the response
        repository.flush();
        return TaskResponse.from(task, today());
    }

    @Transactional
    public TaskResponse changeStatus(long id, TaskStatus next) {
        Task task = load(id);
        TaskStatus current = task.getStatus();
        if (current == next) {
            // idempotent: repeating the same request is not an error
            return TaskResponse.from(task, today());
        }
        if (!current.canMoveTo(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }
        task.setStatus(next);
        task.setCompletedAt(next == TaskStatus.DONE ? clock.instant() : null);
        repository.flush();
        return TaskResponse.from(task, today());
    }

    @Transactional
    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new TaskNotFoundException(id);
        }
    }

    private Task load(long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private static void apply(Task task, TaskRequest request) {
        task.setTitle(request.title().trim());
        task.setDescription(blankToNull(request.description()));
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setDueDate(request.dueDate());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
