package com.phonecat.core;

import com.phonecat.model.ScoringConfig;

/**
 * The core algorithm for calculating a phone number's memorability score.
 * Stateless and thread-safe.
 */
public class PhoneNumberScorer {

    public int calculateScore(String number, ScoringConfig cfg) {
    final int len = number.length();
        // Short-circuit major cases
        if (isAllSameDigits(number)) return 100; // premium
        if (isStrictSequence(number)) return (int) Math.min(100, 95); // treat full-length strict seq as near-premium

        double repetitionScore = repetitionScore(number);
        double sequenceScore = sequenceScore(number);
        double patternScore = patternScore(number);
        double periodicScore = periodicScore(number);
        double alternationScore = alternationScore(number);
        double rhythmScore = rhythmScore(number);
        double uniqueDigitScore = uniqueDigitScore(number);
        double culturalScore = culturalScore(number, cfg);

        double total = 0;
    total += repetitionScore * cfg.wRepetition(len);
    total += sequenceScore * cfg.wSequence(len);
    total += patternScore * cfg.wPattern(len);
    total += periodicScore * cfg.wPeriodic(len);
    total += alternationScore * cfg.wAlternation(len);
    total += rhythmScore * cfg.wRhythm(len);
    total += uniqueDigitScore * cfg.wUnique(len);
        total += culturalScore;

        if (total > 100) total = 100;
        if (total < 0) total = 0;
        return (int) Math.round(total);
    }

    // --- Feature Calculations ---
    private double repetitionScore(String s) {
        // Sum over runs: len(run)^2.5
        double sum = 0;
        int i = 0;
        while (i < s.length()) {
            int j = i + 1;
            while (j < s.length() && s.charAt(j) == s.charAt(i)) j++;
            int run = j - i;
            if (run >= 2) sum += Math.pow(run, 2.5);
            i = j;
        }
        return sum;
    }

    private double sequenceScore(String s) {
        // +15 per ascending/descending window of length 3 (overlapping)
        double sum = 0;
        for (int i = 0; i <= s.length() - 3; i++) {
            int d1 = s.charAt(i) - '0';
            int d2 = s.charAt(i + 1) - '0';
            int d3 = s.charAt(i + 2) - '0';
            if (d2 - d1 == 1 && d3 - d2 == 1) sum += 15;
            if (d1 - d2 == 1 && d2 - d3 == 1) sum += 15;
        }
        return sum;
    }

    private double patternScore(String s) {
        double score = 0;
        // Palindrome
        if (isPalindrome(s)) score += 25;
        // 4-digit classic patterns
        if (s.length() == 4) {
            if (s.matches("(\\d)\\1(\\d)\\2")) score += 20; // AABB
            if (s.matches("(\\d)(\\d)\\1\\2")) score += 20; // ABAB
        }
        // Pairwise blocks for even lengths (AABBCC...)
        if (s.length() % 2 == 0) {
            boolean pairsAllEqual = true;
            for (int i = 0; i < s.length(); i += 2) {
                if (s.charAt(i) != s.charAt(i + 1)) { pairsAllEqual = false; break; }
            }
            if (pairsAllEqual) score += 14;
        }
        return score;
    }

    private double periodicScore(String s) {
        int p = minimalPeriod(s);
        if (p < s.length()) {
            return (s.length() / (double) p - 1) * 12; // boost proportional to repetitions
        }
        return 0;
    }

    private double alternationScore(String s) {
        int p = minimalPeriod(s);
        if (p == 2 && s.charAt(0) != s.charAt(1)) return 15;
        return 0;
    }

    private double rhythmScore(String s) {
        int runs = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != s.charAt(i - 1)) runs++;
        }
        return (s.length() / (double) runs) * 6;
    }

    private double uniqueDigitScore(String s) {
        boolean[] seen = new boolean[10];
        int unique = 0;
        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            if (!seen[d]) { seen[d] = true; unique++; }
        }
        return (s.length() - unique) * 5;
    }

    private double culturalScore(String s, ScoringConfig cfg) {
        double score = 0;
        if (cfg.luckyTokens != null) {
            for (String tok : cfg.luckyTokens) {
                if (tok != null && !tok.isEmpty() && s.contains(tok)) score += cfg.luckyDigitBonus;
            }
        }
        if (cfg.unluckyTokens != null) {
            for (String tok : cfg.unluckyTokens) {
                if (tok != null && !tok.isEmpty() && s.contains(tok)) score += cfg.unluckyDigitPenalty;
            }
        }
        // Backward single-digit consideration (once each)
        boolean addedLucky = false, addedUnlucky = false;
        if (cfg.luckyDigits != null) {
            for (int i = 0; i < s.length(); i++) {
                if (cfg.luckyDigits.contains(s.charAt(i))) { score += cfg.luckyDigitBonus; addedLucky = true; break; }
            }
        }
        if (cfg.unluckyDigits != null) {
            for (int i = 0; i < s.length(); i++) {
                if (cfg.unluckyDigits.contains(s.charAt(i))) { score += cfg.unluckyDigitPenalty; addedUnlucky = true; break; }
            }
        }
        return score;
    }

    // --- Helpers ---
    private boolean isAllSameDigits(String n) { return n.chars().distinct().count() == 1; }

    private boolean isStrictSequence(String n) {
        boolean asc = true, desc = true;
        for (int i = 0; i < n.length() - 1; i++) {
            if (n.charAt(i + 1) != n.charAt(i) + 1) asc = false;
            if (n.charAt(i + 1) != n.charAt(i) - 1) desc = false;
        }
        return asc || desc;
    }

    private boolean isPalindrome(String n) { return new StringBuilder(n).reverse().toString().equals(n); }

    private int minimalPeriod(String s) {
        for (int p = 1; p < s.length(); p++) {
            if (s.length() % p != 0) continue;
            String unit = s.substring(0, p);
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length() / p; i++) sb.append(unit);
            if (sb.toString().equals(s)) return p;
        }
        return s.length();
    }
}
