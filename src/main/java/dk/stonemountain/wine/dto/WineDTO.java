package dk.stonemountain.wine.dto;

import java.time.ZonedDateTime;

import dk.stonemountain.wine.domain.Price;
import dk.stonemountain.wine.domain.Wine;
import dk.stonemountain.wine.error.EntityNotFoundException;

public record WineDTO(
    WineTypeDTO type,
    Long id,
    String name,
    Integer year,
    CountryDTO country,
    String region,
    String grape,
    Double alcoholPercentage,
    String vendor,
    String vendorUrl,
    String vendorDescription,
    ZonedDateTime purchaseDate,
    PriceDTO purchasePrice,
    PriceDTO salePrice,
    Integer inStock,
    String note,
    Integer rating
) {

    public static record PriceDTO(Double cost, Integer pieces) {
        public PriceDTO(Price price) {
            this(price.cost, price.pieces);
        }
    }

    public WineDTO(Wine wine) {
        this(
            WineTypeDTO.getByDomainType(wine.type).orElseThrow(() -> new EntityNotFoundException("WineType " + wine.type + " not found", wine.id, wine.getClass())),
            wine.id,
            wine.name,
            wine.year,
            CountryDTO.getByDomainType(wine.country).orElseThrow(() -> new EntityNotFoundException("Country " + wine.country + " not found", wine.id, wine.getClass())),
            wine.region,
            wine.grape,
            wine.alcoholPercentage,
            wine.vendor,
            wine.vendorUrl,
            wine.vendorDescription,
            wine.purchaseDate,
            new PriceDTO(wine.purchasePrice),
            new PriceDTO(wine.salePrice),
            wine.inStock,
            wine.note,
            wine.rating
        );
    }
}
