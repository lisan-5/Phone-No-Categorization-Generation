'use strict';

class ScoreConfig {
  constructor() {
    this.repetitionWeight = 1.8;
    this.sequenceWeight = 1.6;
    this.patternWeight = 1.5;
    this.periodicWeight = 1.3;
    this.alternationWeight = 1.4;
    this.rhythmWeight = 1.2;
    this.uniqueDigitWeight = 0.8;
    this.luckyNumbers = new Set(['7', '8']);
    this.unluckyNumbers = new Set(['4']);
    this.cultureProfiles = {
      none: { lucky: {}, unlucky: {} },
      global: {
        lucky: {
          '3': [{ region: 'China & Europe', reason: 'Three wishes / philosophical triads' }],
          '7': [{ region: 'USA & Europe', reason: '7 days of creation, 7 wonders' }],
          '8': [{ region: 'China & Japan', reason: 'Sounds like prosperity' }],
          '9': [{ region: 'China & Norway', reason: 'Imperial/Norse sacred number' }],
          '13': [{ region: 'Italy & China', reason: 'Gambling luck / assured growth' }],
          '15': [{ region: 'Spain & Mexico', reason: 'Considered lucky' }],
          '39': [{ region: 'Catholic countries', reason: 'Angel number / guidance' }],
          '666': [{ region: 'China', reason: "Sounds like 'everything goes smoothly'" }]
        },
        unlucky: {
          '4': [{ region: 'China & Japan', reason: "Sounds like 'death'" }],
          '7': [{ region: 'China & SE Asia', reason: 'Ghost month (7th month)' }],
          '8': [{ region: 'India (South)', reason: 'Associated with Sani / inauspicious' }],
          '9': [{ region: 'Japan', reason: "Sounds like 'suffering'" }],
          '13': [{ region: 'Western countries', reason: 'Last Supper / Friday the 13th' }],
          '39': [{ region: 'Afghanistan', reason: "Sounds like 'dead cow' (morda-gow)" }],
          '666': [{ region: 'Christian countries', reason: "Biblical 'Number of the Beast'" }]
        }
      },
      western: {
        lucky: { '3': [{ region: 'USA & Europe', reason: 'All good things in three' }], '7': [{ region: 'USA & Europe', reason: 'Cultural significance of 7' }] },
        unlucky: { '13': [{ region: 'USA & UK', reason: 'Friday the 13th / omens' }], '666': [{ region: 'Christian', reason: 'Number of the Beast' }] }
      },
      china: {
        lucky: { '3': [{ region: 'China', reason: 'Philosophical triads' }], '8': [{ region: 'China', reason: 'Prosperity / wealth' }], '9': [{ region: 'China', reason: "Emperor's number" }], '666': [{ region: 'China', reason: 'Everything goes smoothly' }], '7': [{ region: 'Some regions', reason: 'Cultural usage' }] },
        unlucky: { '4': [{ region: 'China', reason: 'Sounds like death' }], '7': [{ region: 'China', reason: 'Ghost month (7th month)' }] }
      },
      japan: {
        lucky: { '8': [{ region: 'Japan', reason: 'Prosperity sound-alike' }], '3': [{ region: 'Japan', reason: 'Triad harmony' }] },
        unlucky: { '4': [{ region: 'Japan', reason: 'Sounds like death' }], '9': [{ region: 'Japan', reason: 'Sounds like suffering' }] }
      },
      vietnam: {
        lucky: { '8': [{ region: 'Vietnam', reason: 'Chinese influence: prosperity' }], '3': [{ region: 'Vietnam', reason: 'Cultural positive' }] },
        unlucky: { '7': [{ region: 'Vietnam', reason: 'Ghost month (7th month)' }], '4': [{ region: 'Vietnam', reason: 'Death sound-alike (Chinese influence)' }] }
      },
      india_south: {
        lucky: { '3': [{ region: 'India', reason: 'Trinity concepts' }] },
        unlucky: { '8': [{ region: 'India (South)', reason: 'Linked to Sani' }] }
      },
      italy: {
        lucky: { '13': [{ region: 'Italy', reason: "Gambling luck; 'fare tredici'" }], '3': [{ region: 'Italy', reason: 'Triangle strength / tradition' }] },
        unlucky: {}
      },
      germany: {
        lucky: { '4': [{ region: 'Germany', reason: 'Four-leaf clover association' }] },
        unlucky: {}
      },
      spain_mexico: {
        lucky: { '15': [{ region: 'Spain & Mexico', reason: 'Considered lucky' }], '3': [{ region: 'Spain & Mexico', reason: 'Cultural positive' }] },
        unlucky: {}
      },
      norway: {
        lucky: { '9': [{ region: 'Norway', reason: 'Norse sacred number' }] },
        unlucky: {}
      },
      afghanistan: {
        lucky: {},
        unlucky: { '39': [{ region: 'Afghanistan', reason: "Sounds like 'dead cow' (morda-gow)" }] }
      },
      catholic: {
        lucky: { '39': [{ region: 'Catholic countries', reason: 'Angel number / guidance' }], '3': [{ region: 'Catholic', reason: 'Holy Trinity' }] },
        unlucky: { '666': [{ region: 'Christian', reason: 'Number of the Beast' }] }
      },
      russia: {
        lucky: {},
        unlucky: { even: [{ region: 'Russia', reason: 'Even counts used for funerals' }] }
      },
      ethiopia: {
  lucky: {},
  unlucky: { '666': [{ region: 'Ethiopia', reason: 'Considered inauspicious' }] }
      }
    };
  }

  updateFromUI() {
    this.repetitionWeight = parseFloat(document.getElementById('weight-repetition').value);
    this.sequenceWeight = parseFloat(document.getElementById('weight-sequence').value);
    this.patternWeight = parseFloat(document.getElementById('weight-pattern').value);
    this.periodicWeight = parseFloat(document.getElementById('weight-periodic').value);
    this.alternationWeight = parseFloat(document.getElementById('weight-alternation').value);
    this.rhythmWeight = parseFloat(document.getElementById('weight-rhythm').value);
    this.uniqueDigitWeight = parseFloat(document.getElementById('weight-unique').value);
    this.luckyNumbers = new Set(document.getElementById('config-lucky').value.split(',').map(s => s.trim()).filter(Boolean));
    this.unluckyNumbers = new Set(document.getElementById('config-unlucky').value.split(',').map(s => s.trim()).filter(Boolean));
  }
}

class PhoneNumberAssessor {
  constructor(config) { this.config = config; }

  /**
   * Calculate a 0-100 score for a digit string.
   * @param {string} number - 4 to 8 digit string.
   * @returns {number} score in [0,100]
   */
  calculateScore(number) {
    let repetitionScore = 0;
    const repMatches = number.match(/(\d)\1+/g) || [];
    repMatches.forEach(match => { repetitionScore += Math.pow(match.length, 2.5); });

    let sequenceScore = 0;
    for (let i = 0; i <= number.length - 3; i++) {
      const d1 = parseInt(number[i]), d2 = parseInt(number[i+1]), d3 = parseInt(number[i+2]);
      if (d2 - d1 === 1 && d3 - d2 === 1) sequenceScore += 15;
      if (d1 - d2 === 1 && d2 - d3 === 1) sequenceScore += 15;
    }
  // Monotonic run bonus: reward the longest strict ±1 run
    let incLen = 1, decLen = 1, longestRun = 1;
    for (let i = 1; i < number.length; i++) {
      const prev = number.charCodeAt(i - 1) - 48;
      const curr = number.charCodeAt(i) - 48;
      const diff = curr - prev;
      if (diff === 1) { incLen += 1; decLen = 1; }
      else if (diff === -1) { decLen += 1; incLen = 1; }
      else { incLen = 1; decLen = 1; }
      if (incLen > longestRun) longestRun = incLen;
      if (decLen > longestRun) longestRun = decLen;
    }
    if (longestRun >= 4) {
      // Add per-step bonus to longest run; scales with sequence weight
      const monotonicPerStep = 4.7;
      sequenceScore += longestRun * monotonicPerStep;
    }

    let patternScore = 0;
    const isPalindrome = number === [...number].reverse().join('');
    if (isPalindrome) patternScore += 25;
    if (number.length % 2 === 0) {
      let pairsAllEqual = true;
      for (let i = 0; i < number.length; i += 2) { if (number[i] !== number[i+1]) { pairsAllEqual = false; break; } }
      if (pairsAllEqual) patternScore += 14;
      if (/(\d)\1(\d)\2/.test(number) || /(\d)(\d)\1\2/.test(number)) patternScore += 20;
    }

    const minimalPeriod = (s) => {
      for (let p = 1; p < s.length; p++) { if (s.length % p !== 0) continue; const unit = s.slice(0,p); if (unit.repeat(s.length/p) === s) return p; }
      return s.length;
    };
    const p = minimalPeriod(number);
    let periodicScore = 0; if (p < number.length) periodicScore = (number.length / p - 1) * 12;
    let alternationScore = 0; if (p === 2 && number[0] !== number[1]) alternationScore = 15;

    let runs = 1; for (let i = 1; i < number.length; i++) { if (number[i] !== number[i-1]) runs++; }
    const rhythmScore = (number.length / runs) * 6;

    const uniqueCount = new Set(number).size;
    const uniqueDigitScore = (number.length - uniqueCount) * 5;

    let culturalScore = 0;
    this.config.luckyNumbers.forEach(lucky => { if (number.includes(lucky)) culturalScore += 5; });
    this.config.unluckyNumbers.forEach(unlucky => { if (number.includes(unlucky)) culturalScore -= 10; });

    let total = repetitionScore*this.config.repetitionWeight +
                sequenceScore*this.config.sequenceWeight +
                patternScore*this.config.patternWeight +
                periodicScore*this.config.periodicWeight +
                alternationScore*this.config.alternationWeight +
                rhythmScore*this.config.rhythmWeight +
                uniqueDigitScore*this.config.uniqueDigitWeight +
                culturalScore;
    if (total > 100) total = 100;
    return Math.max(0, Math.min(100, total));
  }

  /**
   * Map score to subcategory label.
   * @param {number} score
   * @returns {('Premium'|'Platinum'|'Gold'|'Silver'|'Bronze')}
   */
  getSubcategory(score) {
    if (score >= 95) return 'Premium';
    if (score >= 90) return 'Platinum';
    if (score >= 75) return 'Gold';
    if (score >= 50) return 'Silver';
    return 'Bronze';
  }

  /**
   * Categorize a number by scoring and tier mapping.
   * @param {string} number - 4 to 8 digit string.
   * @returns {{error?:string, number?:string, digit_category?:string, subcategory?:string, score?:number, notes?:Array}}
   */
  categorize(number) {
    if (!/^\d{4,8}$/.test(number)) return { error: 'Input must be a 4 to 8 digit number.' };
    const score = this.calculateScore(number);
    const subcategory = this.getSubcategory(score);
    const culturalNotes = this.getCulturalNotes(number);
    return { number, digit_category: `${number.length}-digit`, subcategory, score: Math.round(score), notes: culturalNotes };
  }

  /**
   * Generate numbers matching a desired subcategory.
   * @param {number} digitLength - 4..8
   * @param {string} subcategory - Premium|Platinum|Gold|Silver|Bronze
   * @param {number} count - desired quantity
   * @returns {Array}
   */
  generate(digitLength, subcategory, count) {
    const results = [];
    const candidates = new Set();
    const accepted = new Set();

    // repeats
    for (let i = 0; i < 10; i++) {
      candidates.add(String(i).repeat(digitLength));
      for (let j = 0; j < 10; j++) {
        if (i === j) continue;
        if (digitLength === 4) {
          candidates.add(`${i}${i}${i}${j}`);
          candidates.add(`${i}${i}${j}${j}`);
          candidates.add(`${i}${j}${j}${j}`);
        }
      }
    }

    // palindromes
    const halfLen = Math.ceil(digitLength / 2);
    const start = 10 ** (halfLen - 1);
    const end = 10 ** halfLen;
    for (let i = start; i < end; i++) {
      const firstHalf = String(i);
      const secondHalf = [...firstHalf].reverse().join('');
      const palindrome = digitLength % 2 === 1 ? firstHalf + secondHalf.substring(1) : firstHalf + secondHalf;
      candidates.add(palindrome);
    }

    // sequences
    for (let i = 0; i <= 10 - digitLength; i++) {
      let asc = '', desc = '';
      for (let j = 0; j < digitLength; j++) { asc += (i + j); desc += (9 - i - j); }
      candidates.add(asc); candidates.add(desc);
    }

    // periodic AB
    for (let a = 0; a <= 9; a++) for (let b = 0; b <= 9; b++) {
      if (a === b) continue; const unit = `${a}${b}`;
      candidates.add(unit.repeat(Math.ceil(digitLength/2)).slice(0, digitLength));
    }
    // periodic ABC
    if (digitLength % 3 === 0) {
      for (let a = 0; a <= 9; a++) for (let b = 0; b <= 9; b++) for (let c = 0; c <= 9; c++) {
        if (a===b || b===c || a===c) continue;
        const unit = `${a}${b}${c}`; candidates.add(unit.repeat(digitLength/3));
      }
    }

    // even-length pairwise blocks
    if (digitLength % 2 === 0) {
      const pairs = digitLength / 2;
      for (let a = 0; a <= 9; a++) for (let b = 0; b <= 9; b++) {
        if (pairs >= 2) {
          let s = `${a}${a}${b}${b}`; if (pairs === 2) candidates.add(s);
          if (pairs >= 3) for (let c = 0; c <= 9; c++) { if (c===a||c===b) continue; s = `${a}${a}${b}${b}${c}${c}`; if (pairs===3) candidates.add(s); }
        }
      }
    }

    // filter
    candidates.forEach(numStr => {
      const score = this.calculateScore(numStr);
      if (this.getSubcategory(score) === subcategory && !accepted.has(numStr)) { results.push(this.categorize(numStr)); accepted.add(numStr); }
    });

    // random fill
    let attempts = 0; const maxAttempts = 500000;
    const min = 10 ** (digitLength - 1); const max = (10 ** digitLength) - 1;
    while (results.length < count && attempts < maxAttempts) {
      const randInt = Math.floor(Math.random() * (max - min + 1)) + min;
      const numStr = String(randInt).padStart(digitLength, '0');
      if (!candidates.has(numStr)) {
        const score = this.calculateScore(numStr);
        if (this.getSubcategory(score) === subcategory && !accepted.has(numStr)) { results.push(this.categorize(numStr)); accepted.add(numStr); }
        candidates.add(numStr);
      }
      attempts++;
    }

    const uniqueResults = Array.from(new Map(results.map(item => [item.number, item])).values());
    uniqueResults.sort((a,b) => b.score - a.score);
    return uniqueResults.slice(0, count);
  }

  /**
   * Collect cultural notes for tokens present in the number for the active profile.
   * @param {string} number
   * @returns {Array<{type:'lucky'|'unlucky', token:string, region:string, reason:string}>}
   */
  getCulturalNotes(number) {
    const notes = [];
    const profileKey = document.getElementById('region-preset')?.value || 'global';
    const profile = (this.config.cultureProfiles && this.config.cultureProfiles[profileKey]) || this.config.cultureProfiles.global;

    const contains = (token) => number.includes(token);
    const addFrom = (bag, label) => {
      Object.keys(bag || {}).forEach(tok => {
        if (tok === 'even') {
          if (parseInt(number[number.length-1]) % 2 === 0) bag[tok].forEach(info => notes.push({ type: label, token: tok, region: info.region, reason: info.reason }));
        } else if (contains(tok)) {
          bag[tok].forEach(info => notes.push({ type: label, token: tok, region: info.region, reason: info.reason }));
        }
      });
    };
    addFrom(profile.lucky, 'lucky');
    addFrom(profile.unlucky, 'unlucky');
    return notes;
  }
}

// UI wiring
(function init() {
  document.addEventListener('DOMContentLoaded', () => {
    const config = new ScoreConfig();
    const assessor = new PhoneNumberAssessor(config);

    const tabs = {
      categorize: document.getElementById('tab-categorize'),
      generate: document.getElementById('tab-generate'),
      config: document.getElementById('tab-config'),
      docs: document.getElementById('tab-docs'),
    };
    const panels = {
      categorize: document.getElementById('panel-categorize'),
      generate: document.getElementById('panel-generate'),
      config: document.getElementById('panel-config'),
      docs: document.getElementById('panel-docs'),
    };
  // Categorize tab outputs
  const catResultsOutput = document.getElementById('cat-results-output');
  // Categorize tab JSON controls
  const catResultsJson = document.getElementById('cat-results-json');
  const catToggleJson = document.getElementById('cat-toggle-json');
  const catCopyJsonBtn = document.getElementById('cat-copy-json');
  // Generate tab outputs
  const genResultsOutput = document.getElementById('gen-results-output');
  const genResultsJson = document.getElementById('gen-results-json');
  const genToggleJson = document.getElementById('gen-toggle-json');
  const genCopyJsonBtn = document.getElementById('gen-copy-json');

    function switchTab(tabName) {
      Object.values(tabs).forEach(tab => tab.classList.replace('tab-active', 'tab-inactive'));
      Object.values(panels).forEach(panel => panel.classList.add('hidden'));
      tabs[tabName].classList.replace('tab-inactive', 'tab-active');
      panels[tabName].classList.remove('hidden');

      const indicator = document.getElementById('tab-indicator');
      const activeBtn = tabs[tabName];
      if (indicator && activeBtn) {
        const rect = activeBtn.getBoundingClientRect();
        const parentRect = activeBtn.parentElement.getBoundingClientRect();
        indicator.style.left = `${rect.left - parentRect.left}px`;
        indicator.style.width = `${rect.width}px`;
      }
    }

    Object.keys(tabs).forEach(tabName => { tabs[tabName].addEventListener('click', () => switchTab(tabName)); });

    function displayGenResults(data) {
      genResultsOutput.innerHTML = '';
      if (!data || (Array.isArray(data) && data.length === 0)) {
        genResultsOutput.innerHTML = `<div class=\"col-span-full text-center py-6 text-gray-500\"><svg class=\"mx-auto mb-2\" width=\"40\" height=\"40\" viewBox=\"0 0 24 24\" fill=\"none\"><circle cx=\"12\" cy=\"12\" r=\"10\" stroke=\"#e5e7eb\" stroke-width=\"3\"/><path d=\"M22 12a10 10 0 00-10-10\" stroke=\"#6366f1\" stroke-width=\"3\" stroke-linecap=\"round\"/></svg>Results will appear here.</div>`;
        genResultsJson.textContent = '';
        genResultsJson.classList.add('hidden');
        return;
      }
      const items = Array.isArray(data) ? data : [data];

      items.forEach(item => {
        if (item.error) {
          genResultsOutput.innerHTML = `<div class=\"bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-lg\">${item.error}</div>`;
          genResultsJson.textContent = JSON.stringify(item, null, 2);
          genResultsJson.classList.toggle('hidden', !genToggleJson.checked);
          return;
        }

        const scoreColor = item.score >= 90 ? 'text-purple-600' : item.score >= 75 ? 'text-amber-500' : item.score >= 50 ? 'text-sky-600' : 'text-gray-500';
        const tier = item.subcategory.toLowerCase();
        const badgeClass = tier === 'premium' ? 'badge badge-premium' : tier === 'platinum' ? 'badge badge-platinum' : tier === 'gold' ? 'badge badge-gold' : tier === 'silver' ? 'badge badge-silver' : 'badge badge-bronze';
        const pct = Math.max(0, Math.min(100, item.score));
        const circumference = 2 * Math.PI * 18; const offset = circumference * (1 - pct/100);
        const explanations = (item.notes || []).slice(0,3).map(n => {
          const badge = n.type === 'lucky' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700';
          return `<span class=\"${badge} text-xs px-2 py-0.5 rounded mr-1 whitespace-nowrap\">${n.token} • ${n.region}</span>`;
        }).join('');

        const card = `
        <div class=\"result-card bg-white/80 p-4 rounded-lg shadow-sm border border-gray-200 flex items-center justify-between hover:shadow transition\">
          <div class=\"flex-1 pr-4\">
            <div class=\"flex items-center gap-2\">
              <p class=\"font-mono text-lg font-semibold text-gray-900 tracking-wide\">${item.number}</p>
              <span class=\"${badgeClass}\">${item.subcategory}</span>
            </div>
            <p class=\"text-sm text-gray-600\">${item.digit_category}</p>
            ${explanations ? `<div class=\"mt-1\">${explanations}</div>` : ''}
          </div>
          <div class=\"flex items-center gap-3\">
            <svg width=\"48\" height=\"48\" viewBox=\"0 0 48 48\" class=\"shrink-0\">
              <circle cx=\"24\" cy=\"24\" r=\"18\" stroke=\"#e5e7eb\" stroke-width=\"6\" fill=\"none\" />
              <circle cx=\"24\" cy=\"24\" r=\"18\" stroke=\"url(#g${item.number})\" stroke-width=\"6\" fill=\"none\" stroke-linecap=\"round\" stroke-dasharray=\"${circumference}\" stroke-dashoffset=\"${offset}\" transform=\"rotate(-90 24 24)\" />
              <defs>
                <linearGradient id=\"g${item.number}\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">
                  <stop offset=\"0%\" stop-color=\"#4f46e5\" />
                  <stop offset=\"100%\" stop-color=\"#7c3aed\" />
                </linearGradient>
              </defs>
            </svg>
            <div class=\"text-right\">
              <p class=\"text-2xl font-bold ${scoreColor}\">${item.score}</p>
              <p class=\"text-xs text-gray-500\">Score</p>
            </div>
          </div>
        </div>`;
        genResultsOutput.innerHTML += card;
      });

      genResultsJson.textContent = JSON.stringify(Array.isArray(data) ? data : items[0], null, 2);
      genResultsJson.classList.toggle('hidden', !genToggleJson.checked);
    }

    document.getElementById('categorize-btn').addEventListener('click', () => {
      const number = document.getElementById('categorize-input').value;
      const result = assessor.categorize(number);
      // Render a single card in the categorize tab
      const render = (item) => {
        if (!catResultsOutput) return;
        catResultsOutput.innerHTML = '';
        if (item.error) {
          catResultsOutput.innerHTML = `<div class=\"bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-lg\">${item.error}</div>`;
          return;
        }
        const scoreColor = item.score >= 90 ? 'text-purple-600' : item.score >= 75 ? 'text-amber-500' : item.score >= 50 ? 'text-sky-600' : 'text-gray-500';
        const tier = item.subcategory.toLowerCase();
        const badgeClass = tier === 'premium' ? 'badge badge-premium' : tier === 'platinum' ? 'badge badge-platinum' : tier === 'gold' ? 'badge badge-gold' : tier === 'silver' ? 'badge badge-silver' : 'badge badge-bronze';
        const pct = Math.max(0, Math.min(100, item.score));
        const circumference = 2 * Math.PI * 18; const offset = circumference * (1 - pct/100);
        const explanations = (item.notes || []).slice(0,3).map(n => {
          const badge = n.type === 'lucky' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700';
          return `<span class=\"${badge} text-xs px-2 py-0.5 rounded mr-1 whitespace-nowrap\">${n.token} • ${n.region}</span>`;
        }).join('');
        const card = `
          <div class=\"result-card bg-white/80 p-4 rounded-lg shadow-sm border border-gray-200 flex items-center justify-between hover:shadow transition\">
            <div class=\"flex-1 pr-4\">
              <div class=\"flex items-center gap-2\">
                <p class=\"font-mono text-lg font-semibold text-gray-900 tracking-wide\">${item.number}</p>
                <span class=\"${badgeClass}\">${item.subcategory}</span>
              </div>
              <p class=\"text-sm text-gray-600\">${item.digit_category}</p>
              ${explanations ? `<div class=\"mt-1\">${explanations}</div>` : ''}
            </div>
            <div class=\"flex items-center gap-3\">
              <svg width=\"48\" height=\"48\" viewBox=\"0 0 48 48\" class=\"shrink-0\">
                <circle cx=\"24\" cy=\"24\" r=\"18\" stroke=\"#e5e7eb\" stroke-width=\"6\" fill=\"none\" />
                <circle cx=\"24\" cy=\"24\" r=\"18\" stroke=\"url(#g${item.number})\" stroke-width=\"6\" fill=\"none\" stroke-linecap=\"round\" stroke-dasharray=\"${circumference}\" stroke-dashoffset=\"${offset}\" transform=\"rotate(-90 24 24)\" />
                <defs>
                  <linearGradient id=\"g${item.number}\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">
                    <stop offset=\"0%\" stop-color=\"#4f46e5\" />
                    <stop offset=\"100%\" stop-color=\"#7c3aed\" />
                  </linearGradient>
                </defs>
              </svg>
              <div class=\"text-right\">
                <p class=\"text-2xl font-bold ${scoreColor}\">${item.score}</p>
                <p class=\"text-xs text-gray-500\">Score</p>
              </div>
            </div>
          </div>`;
        catResultsOutput.innerHTML = card;
      };
      render(result);
      if (catResultsJson) {
        catResultsJson.textContent = JSON.stringify(result, null, 2);
        catResultsJson.classList.toggle('hidden', !(catToggleJson && catToggleJson.checked));
      }
    });

    document.getElementById('generate-btn').addEventListener('click', () => {
  genResultsOutput.innerHTML = `
      <div class=\"col-span-full flex flex-col items-center justify-center py-8 text-gray-600\">
        <svg class=\"animate-spin h-8 w-8 text-indigo-600 mb-2\" xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\">\n          <circle class=\"opacity-25\" cx=\"12\" cy=\"12\" r=\"10\" stroke=\"currentColor\" stroke-width=\"4\"></circle>\n          <path class=\"opacity-75\" fill=\"currentColor\" d=\"M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z\"></path>\n        </svg>
        Generating numbers...
      </div>`;
      setTimeout(() => {
        const length = parseInt(document.getElementById('generate-length').value);
        const category = document.getElementById('generate-category').value;
        const count = parseInt(document.getElementById('generate-count').value);
  const results = assessor.generate(length, category, count);
  displayGenResults(results);
      }, 50);
    });

    document.querySelectorAll('#panel-config input').forEach(input => {
      input.addEventListener('input', () => {
        config.updateFromUI();
        if (input.type === 'range') {
          const key = input.id.split('-')[1];
          const span = document.getElementById(`val-${key}`);
          if (span) span.textContent = input.value;
        }
      });
    });

    const presetSelect = document.getElementById('region-preset');
    if (presetSelect) {
      presetSelect.addEventListener('change', () => {
        const val = presetSelect.value || 'none';
        const prof = config.cultureProfiles[val] || config.cultureProfiles.none;
        const luckyTokens = Object.keys(prof.lucky || {}).filter(k => k !== 'even');
        const unluckyTokens = Object.keys(prof.unlucky || {}).filter(k => k !== 'even');
        document.getElementById('config-lucky').value = val === 'none' ? '' : luckyTokens.join(', ');
        document.getElementById('config-unlucky').value = val === 'none' ? '' : unluckyTokens.join(', ');
        config.updateFromUI();

        const luckyList = document.getElementById('summary-lucky');
        const unluckyList = document.getElementById('summary-unlucky');
        if (luckyList && unluckyList) {
          luckyList.innerHTML = '';
          unluckyList.innerHTML = '';
          Object.entries(prof.lucky || {}).forEach(([tok, infos]) => {
            infos.forEach(info => {
              const li = document.createElement('li');
              li.innerHTML = `<span class="font-mono font-medium">${tok}</span> — ${info.reason} <span class="text-xs text-gray-500">(${info.region})</span>`;
              luckyList.appendChild(li);
            });
          });
          Object.entries(prof.unlucky || {}).forEach(([tok, infos]) => {
            infos.forEach(info => {
              const li = document.createElement('li');
              li.innerHTML = `<span class="font-mono font-medium">${tok}</span> — ${info.reason} <span class="text-xs text-gray-500">(${info.region})</span>`;
              unluckyList.appendChild(li);
            });
          });
        }
      });
      presetSelect.value = 'none';
      presetSelect.dispatchEvent(new Event('change'));
    }

    if (genToggleJson && genResultsJson) {
      genToggleJson.addEventListener('change', () => { genResultsJson.classList.toggle('hidden', !genToggleJson.checked); });
    }
    if (genCopyJsonBtn && genResultsJson) {
      genCopyJsonBtn.addEventListener('click', async () => {
        try { await navigator.clipboard.writeText(genResultsJson.textContent || ''); genCopyJsonBtn.textContent = 'Copied'; setTimeout(() => genCopyJsonBtn.textContent = 'Copy JSON', 1200); }
        catch { genCopyJsonBtn.textContent = 'Failed'; setTimeout(() => genCopyJsonBtn.textContent = 'Copy JSON', 1200); }
      });
    }

    if (catToggleJson && catResultsJson) {
      catToggleJson.addEventListener('change', () => { catResultsJson.classList.toggle('hidden', !catToggleJson.checked); });
    }
    if (catCopyJsonBtn && catResultsJson) {
      catCopyJsonBtn.addEventListener('click', async () => {
        try { await navigator.clipboard.writeText(catResultsJson.textContent || ''); catCopyJsonBtn.textContent = 'Copied'; setTimeout(() => catCopyJsonBtn.textContent = 'Copy JSON', 1200); }
        catch { catCopyJsonBtn.textContent = 'Failed'; setTimeout(() => catCopyJsonBtn.textContent = 'Copy JSON', 1200); }
      });
    }

    // initialize indicator position for initial active tab
    switchTab('categorize');
  });
})();
