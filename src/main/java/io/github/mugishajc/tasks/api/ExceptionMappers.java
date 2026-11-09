package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.service.InvalidQueryException;
import io.github.mugishajc.tasks.service.TaskNotFoundException;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Turns domain exceptions into JSON error bodies. Bean Validation failures
 * are left to Quarkus, which already answers 400 with a list of violations.
 */
public class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> notFound(TaskNotFoundException e) {
        return error(Status.NOT_FOUND, e.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> badQuery(InvalidQueryException e) {
        return error(Status.BAD_REQUEST, e.getMessage());
    }

    static RestResponse<ErrorResponse> error(Status status, String message) {
        return RestResponse.status(status, ErrorResponse.of(status.getStatusCode(), status.getReasonPhrase(), message));
    }
}
