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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
    @Parameter(name = "id", description = "Unique wine identifier", example = "42")
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
            schema = @Schema(implementation = WineDTO[].class)
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
        @Parameter(name = "start-index", description = "Zero-based page index", example = "0") @QueryParam("start-index") @DefaultValue("0") int pageIndex,
        @Parameter(name = "size", description = "Page size (max items per page)", example = "20") @QueryParam("size") @DefaultValue("20") int pageSize,
        @Parameter(
            name = "order",
            description = "Sorting as repeated params: field[:asc|desc], e.g. order=year:asc&order=country:desc",
            example = "year:asc"
        ) @QueryParam("order") List<String> orderQuery,
        @Parameter(
            name = "filter",
            description = "Filtering as repeated params: field:value, e.g. filter=name:Merlot&filter=country:France",
            example = "country:France"
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

    @Transactional
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "add", summary = "Add wine", description = "Creates a new wine")
    @APIResponse(
        responseCode = "200",
        description = "Wine created",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Wine.class))
    )
    public Wine add(Wine wine) {
        service.persist(wine);
        return wine;
    }

    @Transactional
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "update", summary = "Update wine", description = "Updates an existing wine by its ID")
    @Parameter(name = "id", description = "Unique wine identifier", example = "42")
    @APIResponse(
        responseCode = "200",
        description = "Wine updated",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Wine.class))
    )
    @APIResponse(responseCode = "204", description = "Wine not found")
    public Wine update(@PathParam("id") Long id, WineDTO wineToSave) {
        Wine wine = service.findById(id);
        if (wine != null) {
            wineToSave.toDomain(wine);
            service.persist(wine);
            return wine;
        } else {
            throw new EntityNotFoundException(id, Wine.class);
        }
    }

    @Transactional
    @DELETE
    @Path("{id}")
    @Operation(operationId = "delete", summary = "Delete wine", description = "Deletes an existing wine by its ID")
    @Parameter(name = "id", description = "Unique wine identifier", example = "42")
    @APIResponse(
        responseCode = "200",
        description = "Wine deleted",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Wine.class))
    )
    @APIResponse(responseCode = "404", description = "Wine not found")
    public void delete(@PathParam("id") Long id) {
        Wine wine = service.findById(id);
        if (wine != null) {
            service.delete(wine);
        } else {
            throw new EntityNotFoundException(id, Wine.class);
        }
    }

    @GET
    @Path("/{id}/images")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download wine image", description = "Streams the wine image as a binary file")
    @Parameter(name = "id", description = "Unique wine identifier", example = "42")
    @Parameter(name = "icon", description = "If true, return a small icon-sized image", example = "false")
    @APIResponse(responseCode = "200", description = "Image stream", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
    @APIResponse(responseCode = "404", description = "Wine or image not found")
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
    @Operation(summary = "Upload wine image", description = "Uploads/replaces the wine image from binary content")
    @Parameter(name = "id", description = "Unique wine identifier", example = "42")
    @APIResponse(responseCode = "204", description = "Uploaded")
    @APIResponse(responseCode = "404", description = "Wine not found")
    public void upload(@PathParam("id") Long id, File image) {
        final var wine = service.findById(id);
        service.putImage(wine, image);
    }
}
