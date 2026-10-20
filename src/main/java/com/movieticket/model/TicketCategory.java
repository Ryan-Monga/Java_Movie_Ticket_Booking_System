package com.movieticket.model;

/**
 * Enumeration of available ticket tiers.
 */
public enum TicketCategory {
    REGULAR("Regular", 1.0),
    PREMIUM("Premium", 1.5),
    IMAX("IMAX 3D", 1.8333333333333333);

    private final String displayName;
    private final double priceMultiplier;

    TicketCategory(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
