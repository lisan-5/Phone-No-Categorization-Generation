package com.phonecat.model;

/**
 * Data Transfer Object (DTO) for a single generated number and its score.
 */
public class GeneratedNumber {
    public final String number;
    public final int score;

    public GeneratedNumber(String number, int score) {
        this.number = number;
        this.score = score;
    }
}
