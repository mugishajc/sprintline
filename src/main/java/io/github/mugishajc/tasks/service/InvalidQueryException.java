package io.github.mugishajc.tasks.service;

/**
 * Raised when a list request carries a parameter we cannot make sense of,
 * such as an unknown sort field or a malformed date.
 */
public class InvalidQueryException extends RuntimeException {

    public InvalidQueryException(String message) {
        super(message);
    }
}
