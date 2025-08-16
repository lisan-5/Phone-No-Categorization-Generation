package com.phonecat.model;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Holds all configurable weights and regional settings for scoring.
 * This class can be easily modified or extended.
 */
public class ScoringConfig {

    // --- High-level weighted features (advanced) ---
    public double repetitionWeight = 1.8;
    public double sequenceWeight = 1.6;
    public double patternWeight = 1.5;
    public double periodicWeight = 1.3;
    public double alternationWeight = 1.4;
    public double rhythmWeight = 1.2;
    public double uniqueDigitWeight = 0.8;

    // --- Length-aware multipliers (index by digit length; defaults to 1.0) ---
    // Only indices 4..8 are relevant; others remain 1.0 as placeholders.
    private final double[] repetitionLenMul = new double[9];
    private final double[] sequenceLenMul = new double[9];
    private final double[] patternLenMul = new double[9];
    private final double[] periodicLenMul = new double[9];
    private final double[] alternationLenMul = new double[9];
    private final double[] rhythmLenMul = new double[9];
    private final double[] uniqueLenMul = new double[9];

    // --- Legacy Pattern Weights (compat/short-circuits) ---
    public double allSameDigitsWeight = 100.0;     // e.g., 77777
    public double sequentialWeightLegacy = 95.0;   // e.g., 12345 or 98765 (rarely used now)
    public double palindromeWeightLegacy = 25.0;   // kept for reference
    public double doubleBlockWeightLegacy = 20.0;  // kept for reference
    public double alternatingPairWeightLegacy = 15.0; // kept for reference
    public double repeatedPairWeightLegacy = 10.0;    // kept for reference

    // --- Regional Customization (token-based) ---
    // Legacy per-digit sets (fallback or alongside tokens):
    public Set<Character> luckyDigits = new HashSet<>(Arrays.asList('7', '8'));
    public Set<Character> unluckyDigits = new HashSet<>();

    // Multi-digit token sets (e.g., "13", "666", "39")
    public Set<String> luckyTokens = new HashSet<>(Arrays.asList("7", "8"));
    public Set<String> unluckyTokens = new HashSet<>();

    public double luckyDigitBonus = 5.0;           // Bonus per distinct lucky token/digit present
    public double unluckyDigitPenalty = -10.0;     // Penalty per distinct unlucky token/digit present

    // Optional profile name for UI display
    public String profileName = "none";

    public ScoringConfig() {
        // Initialize all multipliers to 1.0 by default
        for (int i = 0; i < 9; i++) {
            repetitionLenMul[i] = 1.0;
            sequenceLenMul[i] = 1.0;
            patternLenMul[i] = 1.0;
            periodicLenMul[i] = 1.0;
            alternationLenMul[i] = 1.0;
            rhythmLenMul[i] = 1.0;
            uniqueLenMul[i] = 1.0;
        }
        // Sensible defaults by digit length (4..8)
        // Shorter numbers: boost visible patterns and sequences.
        // Longer numbers: boost rhythm/periodicity/unique-digit effects.
        setLenMultipliers(4,
                1.20, // repetition
                1.20, // sequence
                1.30, // pattern
                1.00, // periodic
                1.10, // alternation
                1.00, // rhythm
                0.80  // unique
        );
        setLenMultipliers(5, 1.15, 1.15, 1.20, 1.00, 1.05, 1.05, 0.90);
        setLenMultipliers(6, 1.00, 1.00, 1.10, 1.10, 1.00, 1.10, 1.00);
        setLenMultipliers(7, 0.95, 0.95, 1.00, 1.15, 1.00, 1.15, 1.10);
        setLenMultipliers(8, 0.90, 0.90, 1.00, 1.20, 1.00, 1.20, 1.20);
    }

    private void setLenMultipliers(int len,
                                   double rep, double seq, double pat, double per, double alt, double rhy, double uni) {
        int idx = clampLen(len);
        repetitionLenMul[idx] = rep;
        sequenceLenMul[idx] = seq;
        patternLenMul[idx] = pat;
        periodicLenMul[idx] = per;
        alternationLenMul[idx] = alt;
        rhythmLenMul[idx] = rhy;
        uniqueLenMul[idx] = uni;
    }

    private int clampLen(int len) { return Math.max(0, Math.min(8, len)); }

    // Effective weights for a given length (base weight * length multiplier)
    public double wRepetition(int len) { return repetitionWeight * repetitionLenMul[clampLen(len)]; }
    public double wSequence(int len)   { return sequenceWeight   * sequenceLenMul[clampLen(len)]; }
    public double wPattern(int len)    { return patternWeight    * patternLenMul[clampLen(len)]; }
    public double wPeriodic(int len)   { return periodicWeight   * periodicLenMul[clampLen(len)]; }
    public double wAlternation(int len){ return alternationWeight* alternationLenMul[clampLen(len)]; }
    public double wRhythm(int len)     { return rhythmWeight     * rhythmLenMul[clampLen(len)]; }
    public double wUnique(int len)     { return uniqueDigitWeight* uniqueLenMul[clampLen(len)]; }
}
