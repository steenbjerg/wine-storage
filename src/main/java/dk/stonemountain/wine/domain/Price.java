package dk.stonemountain.wine.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Price {
    public Price() {}
    public Price(Double cost, Integer pieces) {
        this.cost = cost;
        this.pieces = pieces;
    }
    public Double cost;
    public Integer pieces;
}
