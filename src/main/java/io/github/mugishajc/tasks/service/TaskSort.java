package io.github.mugishajc.tasks.service;

import io.quarkus.panache.common.Sort;

import java.util.Map;

/**
 * Parses sort expressions like {@code dueDate}, {@code -priority} or
 * {@code priority,-createdAt}. A leading minus means descending. Only a
 * known set of fields is accepted so clients cannot sort on arbitrary
 * columns.
 */
public final class TaskSort {

    private static final Map<String, String> FIELDS = Map.of(
            "title", "title",
            "status", "status",
            "priority", "priorityWeight",
            "dueDate", "dueDate",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt");

    private TaskSort() {
    }

    public static Sort parse(String expression) {
        if (expression == null || expression.isBlank()) {
            return Sort.descending("createdAt").and("id", Sort.Direction.Descending);
        }
        Sort sort = null;
        for (String part : expression.split(",")) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }
            boolean descending = token.startsWith("-");
            String name = descending ? token.substring(1) : token;
            String column = FIELDS.get(name);
            if (column == null) {
                throw new InvalidQueryException("Cannot sort by '" + name + "'. Allowed: " + String.join(", ", FIELDS.keySet()));
            }
            Sort.Direction direction = descending ? Sort.Direction.Descending : Sort.Direction.Ascending;
            sort = sort == null ? Sort.by(column, direction) : sort.and(column, direction);
        }
        if (sort == null) {
            return parse(null);
        }
        // id as a tie breaker keeps paging stable when many rows share a value
        return sort.and("id", Sort.Direction.Ascending);
    }
}
