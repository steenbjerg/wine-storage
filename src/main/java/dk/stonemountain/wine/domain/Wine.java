package dk.stonemountain.wine.domain;

import java.sql.Blob;
import java.time.ZonedDateTime;

import org.hibernate.annotations.EmbeddedColumnNaming;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Wine extends PanacheEntity {
    public enum WineType {
        RED, WHITE;
    };
    public enum Country {
        ITALY, FRANCE, SPAIN, GERMANY, PORTUGAL;
    };

    @NotNull
    @Enumerated(EnumType.STRING)
    public WineType type;
    @NotNull
    @Size(min = 1, max = 1024)
    public String name;

    public int year;
    @NotNull
    @Enumerated(EnumType.STRING)
    public Country country;
    public String region;
    @NotNull
    public String grape;
    @NotNull
    public Double alcoholPercentage;

    @NotNull
    public String vendor;
    @NotNull
    public String vendorUrl;
    @NotNull
    @Column(columnDefinition = "text")
    public String vendorDescription;

    @NotNull
    public ZonedDateTime purchaseDate;
    @Embedded
    @EmbeddedColumnNaming()
    @NotNull
    public Price purchasePrice;
    @Embedded
    @EmbeddedColumnNaming()
    public Price salePrice;
    @NotNull
    public Integer inStock;
    @Column(columnDefinition = "text")
    public String note;
    @NotNull
    @Size(min = 0, max = 5)
    public Integer rating;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public Blob image;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public Blob icon;
}
