package dk.stonemountain.wine.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Price {
    public Double cost;
    public Integer pieces;
}
