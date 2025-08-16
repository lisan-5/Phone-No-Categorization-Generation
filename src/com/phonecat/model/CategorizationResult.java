package com.phonecat.model;

import com.google.gson.Gson; // For JSON output example

/**
 * Data Transfer Object (DTO) for holding the result of a categorization.
 */
public class CategorizationResult {
    public final String number;
    public final String digit_category;
    public final String subcategory;
    public final int score;

    public CategorizationResult(String number, String digit_category, String subcategory, int score) {
        this.number = number;
        this.digit_category = digit_category;
        this.subcategory = subcategory;
        this.score = score;
    }

    /**
     * Provides a JSON representation of the object.
     * Example: {"number":"1234","digit_category":"4-digit","subcategory":"Gold","score":82}
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return String.format(
            "<html><b>Number:</b> %s<br><b>Category:</b> %s<br><b>Subcategory:</b> %s<br><b>Score:</b> %d</html>",
            number, digit_category, subcategory, score
        );
    }
}
