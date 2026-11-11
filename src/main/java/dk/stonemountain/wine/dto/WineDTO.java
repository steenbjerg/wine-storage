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

    public void toDomain(Wine wine) {
        wine.id = id();
        wine.name = name();
        wine.year = year();
        wine.type = type().getDomainType();
        wine.country = country().getDomainType();
        wine.region = region();
        wine.grape = grape();
        wine.alcoholPercentage = alcoholPercentage();
        wine.vendor = vendor();
        wine.vendorUrl = vendorUrl();
        wine.vendorDescription = vendorDescription();
        wine.purchaseDate = purchaseDate();
        wine.purchasePrice = new Price(purchasePrice().cost(), purchasePrice().pieces());
        wine.inStock = inStock();
        wine.note = note();
        wine.rating = rating();
    }
}
