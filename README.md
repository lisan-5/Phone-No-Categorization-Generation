# Phone Number Categorization & Generation

Single algorithm categorizes 4–8 digit numbers and generates numbers matching requested subcategories using a configurable scoring system. Desktop Java Swing UI with cultural presets and custom tokens.

## Structure

- `src/com/phonecat/model/`
  - `ScoringConfig.java` – weights, cultural tokens, length-aware multipliers
  - `CategorizationResult.java` – result DTO (JSON via Gson optional)
  - `GeneratedNumber.java` – generated number DTO
- `src/com/phonecat/core/`
  - `PhoneNumberScorer.java` – core scoring
  - `Categorizer.java` – validation + mapping to tiers
  - `Generator.java` – template-first generation with ETA
- `src/com/phonecat/ui/MainUI.java` – Swing UI (tabs: Categorize, Generate, Configuration, Docs)
- `src/com/phonecat/Main.java` – entry point (scaled fonts for accessibility)
- `lib/` – third-party jars (e.g., `gson-2.10.1.jar`)

## Algorithm Flow

1) Validate: ensure input is 4–8 digits.
2) Score: weighted sum of features (repetition, sequences, patterns, periodicity, alternation, rhythm, unique digits, cultural). Weights are length-aware for 4–8 digits.
3) Clamp: cap to [0, 100].
4) Map: convert score → subcategory (Premium, Platinum, Gold, Silver, Bronze).

### Scoring Formula

```
total =
  repetitionScore * repetitionWeight +
  sequenceScore   * sequenceWeight   +
  patternScore    * patternWeight    +
  periodicScore   * periodicWeight   +
  alternationScore* alternationWeight+
  rhythmScore     * rhythmWeight     +
  uniqueDigitScore* uniqueDigitWeight+
  culturalScore
total = clamp(total, 0, 100)
```

Features:
- Repetition: sum over runs length(run)^2.5
- Sequences: +15 per ascending/descending window of length ≥ 3 (overlapping)
- Patterns: +25 palindrome; +20 AABB/ABAB (4-digit); +14 pairs-all-equal for even length
- Periodicity: if minimal period p < len → (len/p - 1) * 12
- Alternation: +15 if global AB (period=2, a != b)
- Rhythm: (len / number_of_runs) * 6
- Unique digits: (len - unique_count) * 5
- Cultural: +5 per distinct lucky token; -10 per distinct unlucky token

### Pseudocode

```
categorize(number, config):
  if not isDigits(number) or len not in [4..8]: return error
  score = calculateScore(number, config)
  tier  = tierFromScore(score)
  return { number, digit_category: len+"-digit", subcategory: tier, score: round(score) }

generate(len, tier, count, config):
  C = set()
  addTemplates(C, len)           # repeats, palindromes, sequences, periodic AB/ABC, pairs
  R = []
  for each n in C:
    if tierFromScore(calculateScore(n, config)) == tier: R.add(categorize(n))
  while |R| < count and attempts < MAX:
    n = random len-digit number (no leading 0)
    if n not in C and tierFromScore(score(n)) == tier: R.add(categorize(n))
    C.add(n)
  return top count by score (unique by number)

calculateScore(s, config):
  repetitionScore = sum_over_runs(length(run)^2.5)
  sequenceScore   = 15 per asc/desc window of length 3 (overlapping)
  patternScore    = +25 if palindrome; +20 if AABB/ABAB (4-digit) or pairs-all-equal for even length
  periodicScore   = if minimal period p < len: (len/p - 1) * 12
  alternationScore= +15 if global alternation (period=2, a!=b)
  rhythmScore     = (len / number_of_runs) * 6
  uniqueDigitScore= (len - unique_count) * 5
  culturalScore   = +5 per lucky token; -10 per unlucky token (distinct)
  return weighted sum → clamp to [0,100]
```

## Example Outputs

```
{"number":"1234","digit_category":"4-digit","subcategory":"Gold","score":82}
[
  {"number":"7777","digit_category":"4-digit","subcategory":"Premium","score":100},
  {"number":"1221","digit_category":"4-digit","subcategory":"Gold","score":85}
]
```

## Cultural Presets (examples)

- east_asia: Lucky 6/8/9; Unlucky 4
- japan: Lucky 7; Unlucky 9, 43
- india: Unlucky 8, 26
- afghanistan: Unlucky 39
- bulgaria: Unlucky specific number 0888 888 888 (context)
- italy: Lucky 13; Unlucky 17
- spain_greece: Unlucky 13
- western / ethiopia: Lucky 7; Unlucky 13, 666

## Performance Tips

- Template-first generation drastically narrows search space.
- Use sets to avoid rescoring duplicates; cap random attempts.
- Short-circuit and early-clamp if total ≥ 100.
- UI generation runs in a background worker; progress bar shows phases and ETA.

## Requirements

- Java 17+ (or newer). Gson is optional for JSON export.

## Tech Stack

- Language: Java (JDK 17+)
- UI: Swing (JTabbedPane, JTable, SwingWorker)
- JSON: Gson (optional)
- Build: javac (no external build tool required)

## Build & Run

Without Gson:

```powershell
$root = "c:\Users\lisan\Desktop\Phone number"
$srcs = Get-ChildItem -Recurse -Filter *.java "$root\src" | ForEach-Object { $_.FullName }
if (-not (Test-Path "$root\out")) { New-Item -ItemType Directory -Force -Path "$root\out" | Out-Null }
javac -d "$root\out" $srcs
java -cp "$root\out" com.phonecat.Main
```

With Gson (JSON export enabled):

```powershell
$root = "c:\Users\lisan\Desktop\Phone number"
$srcs = Get-ChildItem -Recurse -Filter *.java "$root\src" | ForEach-Object { $_.FullName }
if (-not (Test-Path "$root\out")) { New-Item -ItemType Directory -Force -Path "$root\out" | Out-Null }
javac -cp ".;lib/gson-2.10.1.jar" -d "$root\out" $srcs
java -cp ".;$root\out;lib/gson-2.10.1.jar" com.phonecat.Main
```

## Notes

- Length-aware weight multipliers adapt scoring for 4–8 digits.
- The Configuration tab includes presets and custom tokens; the status bar reflects the active profile.
- The Generate tab shows phase-based progress and ETA; Nearby Suggestions available in Categorize.
  - Controls are simplified: one Generate button, no pause/resume/cancel.
