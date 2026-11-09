package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.domain.TaskPriority;
import io.github.mugishajc.tasks.domain.TaskStatus;
import io.github.mugishajc.tasks.repository.TaskFilter;
import io.github.mugishajc.tasks.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tasks", description = "Create, track and organise tasks")
public class TaskResource {

    private final TaskService service;

    public TaskResource(TaskService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "List tasks", description = "Filter, sort and page through tasks")
    @APIResponse(responseCode = "200", description = "A page of tasks")
    @APIResponse(responseCode = "400", description = "Invalid filter, sort or paging parameter")
    public PageResponse<TaskResponse> list(
            @Parameter(description = "TODO, IN_PROGRESS, BLOCKED, DONE or CANCELLED")
            @QueryParam("status") String status,
            @Parameter(description = "LOW, MEDIUM, HIGH or URGENT")
            @QueryParam("priority") String priority,
            @Parameter(description = "Due on or before this date (yyyy-MM-dd)")
            @QueryParam("dueBefore") String dueBefore,
            @Parameter(description = "Due on or after this date (yyyy-MM-dd)")
            @QueryParam("dueAfter") String dueAfter,
            @Parameter(description = "Case-insensitive search in title and description")
            @QueryParam("q") String text,
            @Parameter(description = "true for open tasks past their due date, false for the rest")
            @QueryParam("overdue") Boolean overdue,
            @Parameter(description = "Zero-based page index")
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @Parameter(description = "Page size, 1 to 100")
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
            @Parameter(description = "Comma separated fields, prefix with - for descending, e.g. -priority,dueDate")
            @QueryParam("sort") @DefaultValue("-createdAt") String sort) {

        TaskFilter filter = new TaskFilter(
                QueryParams.parseEnum(TaskStatus.class, "status", status),
                QueryParams.parseEnum(TaskPriority.class, "priority", priority),
                QueryParams.parseDate("dueBefore", dueBefore),
                QueryParams.parseDate("dueAfter", dueAfter),
                text,
                overdue);
        return service.list(filter, page, size, sort);
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Task counts by status, plus open and overdue totals")
    public TaskStats stats() {
        return service.stats();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a task by id")
    @APIResponse(responseCode = "404", description = "Task not found")
    public TaskResponse get(@PathParam("id") long id) {
        return service.get(id);
    }

    @POST
    @Operation(summary = "Create a task")
    @APIResponse(responseCode = "201", description = "Task created")
    @APIResponse(responseCode = "400", description = "Validation failed")
    public RestResponse<TaskResponse> create(@Valid @NotNull TaskRequest request, UriInfo uriInfo) {
        TaskResponse created = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return RestResponse.ResponseBuilder.<TaskResponse>created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Replace the editable fields of a task")
    @APIResponse(responseCode = "404", description = "Task not found")
    public TaskResponse update(@PathParam("id") long id, @Valid @NotNull TaskRequest request) {
        return service.update(id, request);
    }

    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Move a task to another status")
    @APIResponse(responseCode = "409", description = "Transition not allowed from the current status")
    public TaskResponse changeStatus(@PathParam("id") long id, @Valid @NotNull StatusChangeRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a task")
    @APIResponse(responseCode = "204", description = "Task deleted")
    @APIResponse(responseCode = "404", description = "Task not found")
    public RestResponse<Void> delete(@PathParam("id") long id) {
        service.delete(id);
        return RestResponse.noContent();
    }
}
