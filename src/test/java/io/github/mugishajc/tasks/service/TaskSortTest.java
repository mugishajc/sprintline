package io.github.mugishajc.tasks.service;

import io.quarkus.panache.common.Sort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskSortTest {

    @Test
    void defaultsToNewestFirst() {
        Sort sort = TaskSort.parse(null);
        assertEquals("createdAt", sort.getColumns().get(0).getName());
        assertEquals(Sort.Direction.Descending, sort.getColumns().get(0).getDirection());
    }

    @Test
    void mapsPriorityToWeightAndAddsTieBreaker() {
        Sort sort = TaskSort.parse("-priority, dueDate");

        assertEquals(3, sort.getColumns().size());
        assertEquals("priorityWeight", sort.getColumns().get(0).getName());
        assertEquals(Sort.Direction.Descending, sort.getColumns().get(0).getDirection());
        assertEquals("dueDate", sort.getColumns().get(1).getName());
        assertEquals(Sort.Direction.Ascending, sort.getColumns().get(1).getDirection());
        assertEquals("id", sort.getColumns().get(2).getName());
    }

    @Test
    void rejectsUnknownFields() {
        assertThrows(InvalidQueryException.class, () -> TaskSort.parse("title,secret"));
    }

    @Test
    void treatsEmptyExpressionAsDefault() {
        assertEquals("createdAt", TaskSort.parse(" , ").getColumns().get(0).getName());
    }
}
