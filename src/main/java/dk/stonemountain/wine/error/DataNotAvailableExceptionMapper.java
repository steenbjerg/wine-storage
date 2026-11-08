package dk.stonemountain.wine.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DataNotAvailableExceptionMapper implements ExceptionMapper<DataNotAvailableException> {
    public static record PageDTO (int index, int size) {}
    public static record FailureMessage(String description, PageDTO page, String entityClass) {};

    @Override
    public Response toResponse(DataNotAvailableException ex) {
        var message = new FailureMessage(ex.getMessage(), new PageDTO(ex.getPage().index, ex.getPage().size), ex.getEntityClass() != null ? ex.getEntityClass().getName() : "Not available");
        return Response.status(Status.NO_CONTENT)
            .entity(message)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
