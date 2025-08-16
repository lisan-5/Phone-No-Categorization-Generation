package com.phonecat.core;

import com.phonecat.model.GeneratedNumber;
import com.phonecat.model.ScoringConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements the "Generation Mode" with template-first strategy, token seeding,
 * cancellation/pause control, progress with ETA, and nearby suggestions.
 */
public class Generator {

    // Control for pause/resume/cancel
    public static class Control {
        private volatile boolean cancelled = false;
        private volatile boolean paused = false;
        public void cancel() { this.cancelled = true; }
        public void pause() { this.paused = true; }
        public void resume() { this.paused = false; }
        public boolean isCancelled() { return cancelled; }
        public boolean isPaused() { return paused; }
    }

    // Progress snapshot
    public static class Progress {
        public final int phase; // 1 = templates, 2 = random
        public final double fraction; // 0..1
        public final int found;
        public final int limit;
        public final long etaMillis;
        public Progress(int phase, double fraction, int found, int limit, long etaMillis) {
            this.phase = phase; this.fraction = fraction; this.found = found; this.limit = limit; this.etaMillis = etaMillis;
        }
    }

    @FunctionalInterface
    public interface ProgressListener { void onProgress(Progress p); }

    public void generate(int digitLength, String subcategory, int limit,
                         Consumer<GeneratedNumber> onNumberFound,
                         Runnable onComplete) {
        generateFast(digitLength, subcategory, limit, new ScoringConfig(), new Control(), null, onNumberFound, onComplete);
    }

    public void generate(int digitLength, String subcategory, int limit, ScoringConfig config,
                         Consumer<GeneratedNumber> onNumberFound,
                         Runnable onComplete) {
        generateFast(digitLength, subcategory, limit, config, new Control(), null, onNumberFound, onComplete);
    }

    public void generateFast(int digitLength, String subcategory, int limit, ScoringConfig config,
                             Control control, ProgressListener progress,
                             Consumer<GeneratedNumber> onNumberFound, Runnable onComplete) {
        final long startNanos = System.nanoTime();
        PhoneNumberScorer scorer = new PhoneNumberScorer();
        Set<String> emitted = new HashSet<>();
        int found = 0;

        // 1) Template candidates
        Set<String> candidates = new HashSet<>();
        addTemplates(candidates, digitLength);
        addTokenSeeded(candidates, digitLength, config);

        int processed = 0;
        int total = Math.max(1, candidates.size());
        for (String cand : candidates) {
            if (control.isCancelled()) break;
            while (control.isPaused()) sleepQuiet(40);

            int score = scorer.calculateScore(cand, config);
            String tier = Categorizer.getSubcategoryForScore(score);
            if (tier.equals(subcategory) && emitted.add(cand)) {
                onNumberFound.accept(new GeneratedNumber(cand, score));
                found++;
                if (found >= limit) break;
            }
            processed++;
            if (progress != null && processed % 128 == 0) {
                double frac = Math.min(1.0, processed / (double) total);
                long eta = etaMillis(startNanos, 0.15 + 0.55 * frac); // assume ~70% weight in phase 1
                progress.onProgress(new Progress(1, frac, found, limit, eta));
            }
        }

        // 2) Random fallback if still needed
        if (!control.isCancelled() && found < limit) {
            Random rng = new Random();
            long min = (long) Math.pow(10, digitLength - 1);
            long max = (long) Math.pow(10, digitLength) - 1;
            int attempts = 0;
            int maxAttempts = 300000; // safety cap
            while (!control.isCancelled() && found < limit && attempts < maxAttempts) {
                while (control.isPaused()) sleepQuiet(40);
                long val = min + (Math.abs(rng.nextLong()) % (max - min + 1));
                String s = String.valueOf(val);
                if (!emitted.contains(s)) {
                    int score = scorer.calculateScore(s, config);
                    String tier = Categorizer.getSubcategoryForScore(score);
                    if (tier.equals(subcategory)) {
                        onNumberFound.accept(new GeneratedNumber(s, score));
                        found++;
                    }
                    emitted.add(s);
                }
                attempts++;
                if (progress != null && attempts % 512 == 0) {
                    double frac = Math.min(1.0, attempts / (double) maxAttempts);
                    long eta = etaMillis(startNanos, 0.7 + 0.3 * frac); // remaining 30% in phase 2
                    progress.onProgress(new Progress(2, frac, found, limit, eta));
                }
            }
        }

        onComplete.run();
    }

    public List<GeneratedNumber> suggestNearby(String number, int count, ScoringConfig config) {
        PhoneNumberScorer scorer = new PhoneNumberScorer();
        int base = scorer.calculateScore(number, config);
        Set<String> variants = new HashSet<>();
        int len = number.length();

        // Palindromize
        String pal = toPalindrome(number);
        if (!pal.equals(number)) variants.add(pal);

        // Strengthen runs: copy neighbor digit to extend runs
        for (int i = 1; i < len; i++) {
            char[] arr = number.toCharArray();
            arr[i] = arr[i - 1];
            variants.add(new String(arr));
        }
        // AB alternation based on first two digits
        if (len >= 2 && number.charAt(0) != number.charAt(1)) {
            char a = number.charAt(0), b = number.charAt(1);
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) sb.append((i % 2 == 0) ? a : b);
            variants.add(sb.toString());
        }
        // Insert lucky tokens at start/end/middle
        for (String tok : config.luckyTokens) {
            if (tok.length() > len) continue;
            int rem = len - tok.length();
            if (rem < 0) continue;
            String left = repeatDigit(number.charAt(0) - '0', rem);
            variants.add(tok + left.substring(0, rem));
            variants.add(left.substring(0, rem / 2) + tok + left.substring(rem / 2));
            variants.add(left.substring(0, rem) + tok);
        }

        // Score and pick better ones
        List<GeneratedNumber> out = new ArrayList<>();
        for (String v : variants) {
            if (v.length() != len) continue;
            int sc = new PhoneNumberScorer().calculateScore(v, config);
            if (sc > base) out.add(new GeneratedNumber(v, sc));
        }
        out.sort((a, b) -> Integer.compare(b.score, a.score));
        if (out.size() > count) return out.subList(0, count);
        return out;
    }

    private void addTemplates(Set<String> out, int len) {
        // All same
        for (int a = 0; a <= 9; a++) out.add(repeatDigit(a, len));

        // Palindromes
        int halfLen = (len + 1) / 2;
        int start = (int) Math.pow(10, halfLen - 1);
        int end = (int) Math.pow(10, halfLen) - 1;
        for (int i = start; i <= end; i++) {
            String fh = String.valueOf(i);
            String sh = new StringBuilder(fh).reverse().toString();
            String pal = (len % 2 == 1) ? fh + sh.substring(1) : fh + sh;
            out.add(pal);
        }

        // Full-length ascending/descending sequences
        for (int i = 0; i <= 10 - len; i++) {
            StringBuilder asc = new StringBuilder();
            StringBuilder desc = new StringBuilder();
            for (int j = 0; j < len; j++) {
                asc.append(i + j);
                desc.append(9 - i - j);
            }
            out.add(asc.toString());
            out.add(desc.toString());
        }

        // AB alternation
        for (int a = 0; a <= 9; a++) {
            for (int b = 0; b <= 9; b++) {
                if (a == b) continue;
                String unit = "" + a + b;
                out.add(repeatString(unit, (len + 1) / 2).substring(0, len));
            }
        }

        // ABC repetition if divisible by 3
        if (len % 3 == 0) {
            for (int a = 0; a <= 9; a++) {
                for (int b = 0; b <= 9; b++) {
                    for (int c = 0; c <= 9; c++) {
                        if (a == b || b == c || a == c) continue;
                        String unit = "" + a + b + c;
                        out.add(repeatString(unit, len / 3));
                    }
                }
            }
        }

        // Pair blocks for even lengths: AABBCC...
        if (len % 2 == 0) {
            int pairs = len / 2;
            for (int a = 0; a <= 9; a++) {
                for (int b = 0; b <= 9; b++) {
                    if (pairs >= 1) {
                        String base = "" + a + a + b + b;
                        if (pairs == 2) out.add(base);
                        if (pairs >= 3) {
                            for (int c = 0; c <= 9; c++) {
                                if (c == a || c == b) continue;
                                if (pairs == 3) out.add("" + a + a + b + b + c + c);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addTokenSeeded(Set<String> out, int len, ScoringConfig cfg) {
        List<String> tokens = new ArrayList<>(cfg.luckyTokens);
        if (tokens.isEmpty()) return;
        for (String tok : tokens) {
            if (tok == null || tok.isEmpty() || tok.length() > len) continue;
            int remaining = len - tok.length();
            for (int d = 0; d <= 9; d++) {
                String pad = repeatDigit(d, remaining);
                out.add(pad.substring(0, remaining/2) + tok + pad.substring(remaining/2));
                out.add(tok + pad);
                out.add(pad + tok);
            }
        }
    }

    private String toPalindrome(String s) {
        int len = s.length();
        String first = s.substring(0, (len + 1) / 2);
        String second = new StringBuilder(first).reverse().toString();
        return (len % 2 == 1) ? first + second.substring(1) : first + second;
    }

    private String repeatDigit(int d, int len) {
        char[] arr = new char[len];
        char c = (char) ('0' + d);
        for (int i = 0; i < len; i++) arr[i] = c;
        return new String(arr);
    }

    private String repeatString(String unit, int times) {
        StringBuilder sb = new StringBuilder(unit.length() * times);
        for (int i = 0; i < times; i++) sb.append(unit);
        return sb.toString();
    }

    private void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private long etaMillis(long startNanos, double fractionComplete) {
        if (fractionComplete <= 0) return -1;
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        long totalMs = (long) (elapsedMs / Math.max(1e-6, fractionComplete));
        return Math.max(0, totalMs - elapsedMs);
    }
}
