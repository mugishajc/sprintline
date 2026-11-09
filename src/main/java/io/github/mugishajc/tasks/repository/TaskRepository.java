package io.github.mugishajc.tasks.repository;

import io.github.mugishajc.tasks.domain.Task;
import io.github.mugishajc.tasks.domain.TaskStatus;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TaskRepository implements PanacheRepository<Task> {

    private static final List<TaskStatus> CLOSED = List.of(TaskStatus.DONE, TaskStatus.CANCELLED);

    /**
     * Builds a query from whichever filter fields are present. Returns a
     * PanacheQuery so the caller decides how to page it.
     */
    public PanacheQuery<Task> search(TaskFilter filter, LocalDate today, Sort sort) {
        List<String> clauses = new ArrayList<>();
        Parameters params = new Parameters();

        if (filter.status() != null) {
            clauses.add("status = :status");
            params.and("status", filter.status());
        }
        if (filter.priority() != null) {
            clauses.add("priority = :priority");
            params.and("priority", filter.priority());
        }
        if (filter.dueBefore() != null) {
            clauses.add("dueDate <= :dueBefore");
            params.and("dueBefore", filter.dueBefore());
        }
        if (filter.dueAfter() != null) {
            clauses.add("dueDate >= :dueAfter");
            params.and("dueAfter", filter.dueAfter());
        }
        if (filter.text() != null && !filter.text().isBlank()) {
            clauses.add("(lower(title) like :text or lower(description) like :text)");
            params.and("text", "%" + filter.text().trim().toLowerCase() + "%");
        }
        if (filter.overdue() != null) {
            if (filter.overdue()) {
                clauses.add("dueDate < :today and status not in :closed");
            } else {
                clauses.add("(dueDate is null or dueDate >= :today or status in :closed)");
            }
            params.and("today", today).and("closed", CLOSED);
        }

        if (clauses.isEmpty()) {
            return findAll(sort);
        }
        return find(String.join(" and ", clauses), sort, params);
    }

    public Map<TaskStatus, Long> countByStatus() {
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, 0L);
        }
        List<Object[]> rows = getEntityManager()
                .createQuery("select t.status, count(t) from Task t group by t.status", Object[].class)
                .getResultList();
        for (Object[] row : rows) {
            counts.put((TaskStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    public long countOverdue(LocalDate today) {
        return count("dueDate < ?1 and status not in ?2", today, CLOSED);
    }
}
