package dk.stonemountain.wine.dto;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import dk.stonemountain.wine.domain.Wine;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

@Schema(enumeration = {"italy", "france", "spain", "germany", "portugal"})
@JsonbTypeAdapter(CountryDTO.CountryDTOAdapter.class)
public enum CountryDTO {
    ITALY(Wine.Country.ITALY, "italy"), FRANCE(Wine.Country.FRANCE, "france"), SPAIN(Wine.Country.SPAIN, "spain"), GERMANY(Wine.Country.GERMANY, "germany"), PORTUGAL(Wine.Country.PORTUGAL, "portugal");

    private final Wine.Country domainType;
    private final String openApiValue;

    private CountryDTO(Wine.Country domainType, String openApiValue) {
        this.domainType = domainType;
        this.openApiValue = openApiValue;
    }

    public Wine.Country getDomainType() {
        return domainType;
    }

    public String getOpenApiValue() {
        return openApiValue;
    }

    public static Optional<CountryDTO> getByDomainType(Wine.Country type) {
        return Arrays.asList(values()).stream()
            .filter(t -> t.domainType == type)
            .findFirst();
    }

    public static Optional<CountryDTO> getByOpenApiValue(String type) {
        return Arrays.asList(values()).stream()
            .filter(t -> t.getOpenApiValue().equals(type))
            .findFirst();
    }

    public static class CountryDTOAdapter implements JsonbAdapter<CountryDTO, String> {
        @Override
		public String adaptToJson(CountryDTO e) {
			return e.getOpenApiValue();
		}

		@Override
		public CountryDTO adaptFromJson(String value) {
			return CountryDTO.getByOpenApiValue(value).orElseThrow(() -> new IllegalArgumentException("unknown country " + value));
		}
    }
};
