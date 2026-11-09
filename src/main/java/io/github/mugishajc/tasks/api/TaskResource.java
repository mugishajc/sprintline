package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.service.TaskService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;
import java.util.List;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    private final TaskService service;

    public TaskResource(TaskService service) {
        this.service = service;
    }

    @GET
    public List<TaskResponse> list() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public TaskResponse get(@PathParam("id") long id) {
        return service.get(id);
    }

    @POST
    public RestResponse<TaskResponse> create(TaskRequest request, UriInfo uriInfo) {
        TaskResponse created = service.create(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return RestResponse.ResponseBuilder.<TaskResponse>created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public TaskResponse update(@PathParam("id") long id, TaskRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public RestResponse<Void> delete(@PathParam("id") long id) {
        service.delete(id);
        return RestResponse.noContent();
    }
}
