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
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    private final TaskService service;

    public TaskResource(TaskService service) {
        this.service = service;
    }

    @GET
    public PageResponse<TaskResponse> list(
            @QueryParam("status") String status,
            @QueryParam("priority") String priority,
            @QueryParam("dueBefore") String dueBefore,
            @QueryParam("dueAfter") String dueAfter,
            @QueryParam("q") String text,
            @QueryParam("overdue") Boolean overdue,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size,
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
    @Path("/{id}")
    public TaskResponse get(@PathParam("id") long id) {
        return service.get(id);
    }

    @POST
    public RestResponse<TaskResponse> create(@Valid @NotNull TaskRequest request, UriInfo uriInfo) {
        TaskResponse created = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return RestResponse.ResponseBuilder.<TaskResponse>created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public TaskResponse update(@PathParam("id") long id, @Valid @NotNull TaskRequest request) {
        return service.update(id, request);
    }

    @PATCH
    @Path("/{id}/status")
    public TaskResponse changeStatus(@PathParam("id") long id, @Valid @NotNull StatusChangeRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DELETE
    @Path("/{id}")
    public RestResponse<Void> delete(@PathParam("id") long id) {
        service.delete(id);
        return RestResponse.noContent();
    }
}
