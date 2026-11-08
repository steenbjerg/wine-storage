package dk.stonemountain.wine.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {
    public static record FailureMessage(String description, Long id, String entityClass, String reason) {};

    @Override
    public Response toResponse(EntityNotFoundException ex) {
        var message = new FailureMessage(ex.getMessage(), ex.getId(), ex.getEntityClass() != null ? ex.getEntityClass().getName() : "Not available", ex.getReason());
        return Response.status(Status.NOT_FOUND)
                .entity(message)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
