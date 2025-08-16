# Phone Number Categorization & Generation Algorithm

Built as part of an internship project.

A single-page web app that scores 4–8 digit numbers for memorability and:
- Categorizes any number into a tier (Premium, Platinum, Gold, Silver, Bronze).
- Generates numbers of a chosen length that match a requested tier.
- Supports configurable scoring weights and regional/cultural preferences.

Open `index.html` in any modern browser (Chrome, Edge, Firefox). No backend or build needed.

---

## Features
- Two modes
  - Categorize: Type a 4–8 digit number and get its score and tier.
  - Generate: Choose digit length, tier, and quantity; get matching numbers.
- Configurable scoring
  - Adjust weights for repetition, sequences, patterns, periodicity, alternation, rhythm, unique digits.
  - Cultural preferences (lucky/unlucky tokens) via profiles or manual input.
  - "No cultural preference" is the default (neutral cultural scoring).
- Cultural context in results
  - Numbers annotated with short region/reason badges (when a profile with tokens is selected).
- In-app documentation
  - Flow, formula, and pseudocode embedded in the Documentation tab.
- Polished UI
  - Clean layout, animated tab indicator, gradient buttons, spinner during generation.
- JSON output
  - Toggle to view/copy JSON for easy export.

---

## How it works (high level)
One core class (`PhoneNumberAssessor`) provides both:
- `categorize(number)` → score + tier
- `generate(length, subcategory, count)` → list of numbers matching the tier

### Scoring factors
For a digit string `s`:
- Repetition: Sum over repeated runs; each run contributes `runLength^2.5` (heavily rewards long repeats).

- Patterns:
  - Palindrome: `+25`.
  - Classic blocks: `+20` if AABB or ABAB (4-digit), and `+14` bonus for pairwise-equal across pairs in even lengths (e.g., AABBCC).
- Periodicity: Compute minimal period `p`; if `p < len`, add `(len/p - 1) * 12`.
- Alternation: `+15` when global period is 2 and A != B (ABAB…).
- Rhythm (chunkability): `(len / number_of_runs) * 6` (fewer runs → higher score).
- Unique digits: `(len - uniqueCount) * 5`.
- Cultural tokens:
  - `+5` per distinct lucky token present in the number.
  - `-10` per distinct unlucky token present in the number.

Weighted sum (with sliders in Configuration):
```
score =
  repetitionScore * repetitionWeight +
  sequenceScore   * sequenceWeight   +
  patternScore    * patternWeight    +
  periodicScore   * periodicWeight   +
  alternationScore* alternationWeight+
  rhythmScore     * rhythmWeight     +
  uniqueDigitScore* uniqueDigitWeight+
  culturalScore
```
Clamped to `[0, 100]` (with early clamp to cap runaway totals).

### Tiers
- Premium: `95–100`
- Platinum: `90–94`
- Gold: `75–89`
- Silver: `50–74`
- Bronze: `< 50`

---

## Generation strategy
- Template-first enumeration of high-value candidates:
  - Repeats (AAAA…); plus AAAB, AABB, ABBB for 4-digit.
  - Palindromes (constructed from half, no leading zeros in the half).
  - Sequences (ascending/descending) for the chosen length.
  - Periodic patterns (AB…), and ABCABC… when length is a multiple of 3.
  - Pairwise blocks (AABB, AABBCC…) for even lengths.
- Score and keep only those that map to the requested tier.
- If still short, random search within the length’s numeric range (no leading zeros) with a safety cap.
- Duplicates are prevented using an `accepted` set and de-duplication at the end; final results sorted by score (desc).

Performance tips:
- Template-first drastically reduces search space.
- Use Sets to avoid rescoring and to prevent duplicates.
- Early clamp at 100 avoids unnecessary work.
- For very large offline batches, parallelize scoring.

---

## Cultural profiles
- Default: `No cultural preference` (tokens cleared; no cultural notes or scoring effects).
- Profiles include: Global, Western, China, Japan, Vietnam, India (South), Italy, Germany, Spain & Mexico, Norway, Afghanistan, Catholic Countries, Russia, Ethiopia (placeholder).
- Selecting a profile:
  - Auto-fills the Lucky/Unlucky inputs with tokens like `3, 7, 8, 39, 666, 13, 4…`.
  - Shows a brief reason per token in the summary panel.
  - Cultural badges appear under results when numbers include those tokens.
- You can adjust tokens manually at any time.

Note: The “Ethiopia” profile now treats `666` as unlucky (inauspicious). You can extend it with additional local beliefs.

---

## Using the app
1. Open `index.html`.
2. Tabs:
   - Categorize: Enter a 4–8 digit number → Analyze.
   - Generate: Choose digit length, subcategory, and quantity → Generate.
   - Configuration: Adjust weights; pick Cultural Profile or use "No cultural preference"; edit Lucky/Unlucky tokens.
   - Documentation: View flow, formula, and pseudocode.
3. Results:
   - Cards show the number, length category, tier badge, circular score ring, and cultural badges (if any).
   - Toggle “Show JSON” to view or copy raw data (includes `notes`).

### Example outputs
Categorize (JSON):
```
{"number":"1234","digit_category":"4-digit","subcategory":"Gold","score":85}
```
Generate (JSON list):
```
[
  {"number":"7777","digit_category":"4-digit","subcategory":"Premium","score":100},
  {"number": "1221", "digit_category": "4-digit", "subcategory": "Silver", "score": 65}
]
```

---

## Code structure
- `index.html` — Markup and layout (Tailwind CSS via CDN). Links `style.css` and `app.js`.
- `app.js` — All application logic:
  - `ScoreConfig`: weights, cultural tokens, profile presets, UI sync.
  - `PhoneNumberAssessor`: scoring, monotonic run bonus for sequences, tier mapping, categorize/generate, cultural notes.
  - UI wiring: tabs, actions, JSON toggle/copy, profile summary, loading spinner.
- `style.css` — Extracted visual styles (font, tab states, badges, card transitions).

### Portability
The algorithm is language-agnostic and easily portable (e.g., to Java). Keep the same scoring factors and mappings; port `ScoreConfig` and `PhoneNumberAssessor` interfaces 1:1.

---

## License & Attribution
- This project was built as part of an internship. Use and adapt as needed for educational and internal purposes.


