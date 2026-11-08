package dk.stonemountain.wine.dto;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import dk.stonemountain.wine.domain.Wine;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@Schema(enumeration = {"red", "white"})
@JsonbTypeAdapter(WineTypeDTO.WineTypeDTOAdapter.class)
public enum WineTypeDTO {
    RED(Wine.WineType.RED, "red"),
    WHITE(Wine.WineType.WHITE, "white");

    private final Wine.WineType domainType;
    private final String openApiValue;

    private WineTypeDTO(Wine.WineType domainType, String openApiValue) {
        this.domainType = domainType;
        this.openApiValue = openApiValue;
    }

    public Wine.WineType getDomainType() {
        return domainType;
    }

    public String getOpenApiValue() {
        return openApiValue;
    }

    public static Optional<WineTypeDTO> getByDomainType(Wine.WineType type) {
        return Arrays.asList(values()).stream()
            .filter(t -> t.domainType == type)
            .findFirst();
    }

    public static Optional<WineTypeDTO> getByOpenApiValue(String type) {
        return Arrays.asList(values()).stream()
            .filter(t -> t.getOpenApiValue().equals(type))
            .findFirst();
    }

    public static class WineTypeDTOAdapter implements JsonbAdapter<WineTypeDTO, String> {
        @Override
		public String adaptToJson(WineTypeDTO e) {
			return e.getOpenApiValue();
		}

		@Override
		public WineTypeDTO adaptFromJson(String value) {
			return WineTypeDTO.getByOpenApiValue(value).orElseThrow(() -> new IllegalArgumentException("unknown wine type value " + value));
		}
    }
}
