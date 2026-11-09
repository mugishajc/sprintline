package io.github.mugishajc.tasks.domain;

/**
 * Task priority. The weight is persisted alongside the name so that sorting
 * by priority follows business order instead of alphabetical order.
 */
public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4);

    private final int weight;

    TaskPriority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
