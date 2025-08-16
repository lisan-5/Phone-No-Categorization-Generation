package com.phonecat.core;

import com.phonecat.model.CategorizationResult;
import com.phonecat.model.ScoringConfig;

/**
 * Implements the "Categorization Mode". It uses the PhoneNumberScorer
 * to determine the category of a given number.
 */
public class Categorizer {

    private final PhoneNumberScorer scorer;
    private ScoringConfig config;

    public Categorizer() {
        this.scorer = new PhoneNumberScorer();
        this.config = new ScoringConfig(); // Uses default config
    }

    public Categorizer(ScoringConfig config) {
        this.scorer = new PhoneNumberScorer();
        this.config = (config != null) ? config : new ScoringConfig();
    }

    public void setConfig(ScoringConfig config) {
        if (config != null) this.config = config;
    }

    public ScoringConfig getConfig() {
        return this.config;
    }

    public CategorizationResult categorize(String number) {
        if (number == null || !number.matches("\\d{4,8}")) {
            throw new IllegalArgumentException("Input must be a valid 4 to 8 digit number.");
        }

        int score = scorer.calculateScore(number, config);
        String subcategory = getSubcategoryForScore(score);
        String digitCategory = number.length() + "-digit";

        return new CategorizationResult(number, digitCategory, subcategory, score);
    }

    /**
     * Assigns a subcategory name based on the score ranges.
     * @param score The calculated score.
     * @return The subcategory string (e.g., "Premium", "Gold").
     */
    public static String getSubcategoryForScore(int score) {
        if (score >= 95) return "Premium";
        if (score >= 90) return "Platinum";
        if (score >= 75) return "Gold";
        if (score >= 50) return "Silver";
        return "Bronze";
    }
}
