package dk.stonemountain.wine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.wine.domain.Wine;
import dk.stonemountain.wine.dto.WineDTO;
import dk.stonemountain.wine.error.DataNotAvailableExceptionMapper;
import dk.stonemountain.wine.error.EntityNotFoundException;
import dk.stonemountain.wine.error.EntityNotFoundExceptionMapper;
import dk.stonemountain.wine.services.WineService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@Path("wines")
@ApplicationScoped
@Tag(name = "Wine")
@Transactional
public class WineResource {
    private static final Logger LOG = LoggerFactory.getLogger(WineResource.class);

    @Inject
    WineService service;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "findById", summary = "Find wine by ID", description = "Returns a wine by its ID")
    @APIResponse(
        responseCode = "200",
        description = "Wine found",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = WineDTO.class))
    )
    @APIResponse(
        responseCode = "404",
        description = "Wine not found",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = EntityNotFoundExceptionMapper.FailureMessage.class)
        )
    )
    public WineDTO get(@PathParam("id") Long id) {
        return Optional.ofNullable(service.findById(id))
            .map(WineDTO::new)
            .orElseThrow(() -> new EntityNotFoundException(id, Wine.class));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        operationId = "findAll",
        summary = "Find all wines",
        description = "Returns a page of wines sorted in accordance with the sortQuery parameter"
    )
    @APIResponse(
        responseCode = "200",
        description = "Wines found",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(
                type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY,
                implementation = WineDTO.class
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Data not available",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = DataNotAvailableExceptionMapper.FailureMessage.class)
        )
    )
    public List<WineDTO> list(
        @Parameter(name = "start-index") @QueryParam("start-index") @DefaultValue("0") int pageIndex,
        @Parameter(name = "size") @QueryParam("size") @DefaultValue("20") int pageSize,
        @Parameter(
            name = "order",
            description = "Order by values are repeated parameters, e.g. ?order=year:asc&order&order=country:desc"
        ) @QueryParam("order") List<String> orderQuery,
        @Parameter(
            name = "filter",
            description = "Filter values as repeated query parameters, e.g. ?filter=name:Merlot&filter=country:France"
        ) @QueryParam("filter") List<String> filterQuery
    ) {
        LOG.info(
            "finding wines with page {}/{}, filter {} and sorting {}",
            pageIndex,
            pageSize,
            filterQuery,
            orderQuery
        );
        Page page = Page.of(pageIndex, pageSize);
        Sort sort = getSortFromQuery(List.of("name"));
        var wines = service.list(page, sort).stream().map(WineDTO::new).toList();
        LOG.trace("Found {} wines", wines.size());
        return wines;
    }

    private Sort getSortFromQuery(List<String> sortQuery) {
        return Sort.by(sortQuery.toArray(new String[0]));
    }

    // @Transactional
    // @POST
    // @Consumes(MediaType.APPLICATION_JSON)
    // @Produces(MediaType.APPLICATION_JSON)
    // public RestResponse<Wine> add(Wine wine) {
    //     service.persist(wine);
    //     return wine;0
    //     // ... build a new location URL and return 201 response with an entity
    // }

    // @Transactional
    // @PUT
    // @Path("{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // @Produces(MediaType.APPLICATION_JSON)
    // public RestResponse<Wine> update(@PathParam("id") Long id, Wine WineToSave) {
    //     if (service.get(id) == null) {
    //         Wine Wine = service.update(id, WineToSave);
    //         return ResponseBuilder.<Wine>create(204).build();
    //     }
    //     Wine Wine = service.update(id, WineToSave);
    //     // ... build a new location URL and return 201 response with an entity
    // }

    // @Transactional
    // @DELETE
    // @Path("{id}")
    // public RestResponse<Wine> delete(@PathParam("id") Long id) {
    //     return !service.delete(id) ? ResponseBuilder.<Wine>create(404).build() : ResponseBuilder.<Wine>create(204).build();
    // }

    @GET
    @Path("/{id}/images")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getImage(@NotNull @PathParam("id") Long id, @QueryParam("icon") @DefaultValue("false") Boolean icon) {
        LOG.info("Fetching image for wine {} (is icon {})", id, icon);
        StreamingOutput stream = out -> QuarkusTransaction.requiringNew().run(() -> {
            final var wine = service.findById(id);
            try (InputStream in = service.getImage(wine, icon)) {
                in.transferTo(out);
            } catch(IOException e){
                throw new RuntimeException("failed to transfer image", e);
            }
        });

        return Response.ok(stream)
            .header("Content-Disposition", "attachment; filename=\"" + "image_" + id + ".png" + "\"")
            .build();
    }

    @POST
    @Path("/{id}/image")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void upload(@PathParam("id") Long id, File image) {
        final var wine = service.findById(id);
        service.putImage(wine, image);
    }
}
