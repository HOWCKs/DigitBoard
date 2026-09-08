import { getSuggestions } from './dictionary.js';

export class KeyboardEngine {
  constructor(options = {}) {
    this.container = options.container;
    this.textarea = options.textarea;
    this.onTextChange = options.onTextChange || (() => {});
    
    // Preferences
    this.showNumberRow = true;
    this.soundEnabled = true;
    this.hapticEnabled = true;
    this.popupEnabled = true;
    this.suggestionsEnabled = true;
    this.currentTheme = 'neumorphism';
    
    // Keyboard State
    this.shiftState = 0; // 0: unshifted, 1: shifted, 2: caps lock
    this.layoutMode = 'alpha'; // 'alpha', 'symbols', 'symbols_extra'
    this.clipboardHistory = [
      "DigitBoard - O melhor teclado Neumorphism!",
      "CI/CD com build APK automático via GitHub Actions",
      "https://github.com/HOWCKs/DigitBoard"
    ];
    
    // Long press timer for accents
    this.longPressTimer = null;
    this.activeAccentKey = null;

    // Web Audio API Synth for Key Click Sound
    this.initAudio();

    // Layout Definitions
    this.numberRow = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'];
    
    this.alphaRows = [
      ['q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'],
      ['a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ç'],
      ['SHIFT', 'z', 'x', 'c', 'v', 'b', 'n', 'm', 'BACKSPACE'],
      ['?123', 'EMOJI', 'VOCAL', 'SPACE', '.', 'ENTER']
    ];

    this.symbolsRows = [
      ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0'],
      ['@', '#', '$', '%', '&', '-', '+', '(', ')', '/'],
      ['=<', '*', '"', "'", ':', ';', '!', '?', 'BACKSPACE'],
      ['ABC', 'EMOJI', 'CLIPBOARD', 'SPACE', ',', 'ENTER']
    ];

    this.symbolsExtraRows = [
      ['~', '`', '|', '•', '√', 'π', '÷', '×', '¶', '∆'],
      ['£', '¥', '€', '¢', '^', '°', '=', '{', '}', '\\'],
      ['?123', '%', '©', '®', '™', '✓', '[', ']', 'BACKSPACE'],
      ['ABC', 'EMOJI', 'CLIPBOARD', 'SPACE', '...', 'ENTER']
    ];

    this.accentsMap = {
      'a': ['á', 'à', 'ã', 'â', 'ä', 'A'],
      'e': ['é', 'è', 'ê', 'ë'],
      'i': ['í', 'ì', 'î', 'ï'],
      'o': ['ó', 'ò', 'õ', 'ô', 'ö'],
      'u': ['ú', 'ù', 'û', 'ü'],
      'c': ['ç', 'C'],
      'n': ['ñ', 'N']
    };

    this.render();
  }

  initAudio() {
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) {
        this.audioCtx = new AudioCtx();
      }
    } catch (e) {
      console.warn("AudioContext not supported");
    }
  }

  playClickSound() {
    if (!this.soundEnabled || !this.audioCtx) return;
    try {
      if (this.audioCtx.state === 'suspended') {
        this.audioCtx.resume();
      }
      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(600, this.audioCtx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(150, this.audioCtx.currentTime + 0.04);
      gain.gain.setValueAtTime(0.12, this.audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + 0.04);
      osc.connect(gain);
      gain.connect(this.audioCtx.destination);
      osc.start();
      osc.stop(this.audioCtx.currentTime + 0.04);
    } catch (e) {}
  }

  triggerHaptic() {
    if (!this.hapticEnabled) return;
    if (navigator.vibrate) {
      navigator.vibrate(20);
    }
  }

  setTheme(theme) {
    this.currentTheme = theme;
    this.container.className = `keyboard-container theme-${theme}`;
    const tag = document.getElementById('current-theme-tag');
    if (tag) {
      const names = {
        neumorphism: 'Neumorphism Soft UI',
        amoled: 'Escuro AMOLED',
        light: 'Claro / Branco',
        rgb: 'RGB Animado (Chroma)'
      };
      tag.textContent = `Tema: ${names[theme] || theme}`;
    }
  }

  render() {
    this.container.innerHTML = '';

    // Determine current rows
    let currentRows = [];
    if (this.layoutMode === 'symbols') {
      currentRows = this.symbolsRows;
    } else if (this.layoutMode === 'symbols_extra') {
      currentRows = this.symbolsExtraRows;
    } else {
      currentRows = [];
      if (this.showNumberRow) {
        currentRows.push(this.numberRow);
      }
      currentRows.push(...this.alphaRows);
    }

    currentRows.forEach((row, rowIndex) => {
      const rowDiv = document.createElement('div');
      rowDiv.className = 'kb-row';

      row.forEach((keyVal) => {
        const keyBtn = document.createElement('button');
        keyBtn.className = 'kb-key';

        // Display label
        let displayLabel = keyVal;
        if (this.layoutMode === 'alpha' && keyVal.length === 1) {
          displayLabel = (this.shiftState > 0) ? keyVal.toUpperCase() : keyVal.toLowerCase();
        }

        // Apply special styling
        if (['SHIFT', 'BACKSPACE', '?123', '=<', 'ABC', 'EMOJI', 'VOCAL', 'CLIPBOARD'].includes(keyVal)) {
          keyBtn.classList.add('key-special');
        }
        if (keyVal === 'SPACE') {
          keyBtn.classList.add('key-space');
          displayLabel = 'Espaço';
        }
        if (keyVal === 'ENTER') {
          keyBtn.classList.add('key-enter');
          displayLabel = '↵';
        }
        if (keyVal === 'SHIFT') {
          displayLabel = (this.shiftState === 2) ? '⇪' : (this.shiftState === 1 ? '⇧' : '⇧');
          if (this.shiftState > 0) keyBtn.classList.add('pressed');
        }
        if (keyVal === 'BACKSPACE') {
          displayLabel = '⌫';
        }
        if (keyVal === 'EMOJI') displayLabel = '😊';
        if (keyVal === 'VOCAL') displayLabel = '🎙️';
        if (keyVal === 'CLIPBOARD') displayLabel = '📋';

        keyBtn.textContent = displayLabel;

        // Events
        keyBtn.addEventListener('mousedown', (e) => this.handleKeyPressStart(keyVal, keyBtn, e));
        keyBtn.addEventListener('mouseup', () => this.handleKeyPressEnd(keyBtn));
        keyBtn.addEventListener('mouseleave', () => this.handleKeyPressEnd(keyBtn));

        keyBtn.addEventListener('touchstart', (e) => {
          e.preventDefault();
          this.handleKeyPressStart(keyVal, keyBtn, e);
        }, { passive: false });
        keyBtn.addEventListener('touchend', (e) => {
          e.preventDefault();
          this.handleKeyPressEnd(keyBtn);
        });

        rowDiv.appendChild(keyBtn);
      });

      this.container.appendChild(rowDiv);
    });
  }

  handleKeyPressStart(keyVal, keyBtn, event) {
    this.playClickSound();
    this.triggerHaptic();

    keyBtn.classList.add('pressed');

    // Key Popup
    if (this.popupEnabled && keyVal.length === 1) {
      this.showKeyPopup(keyBtn, keyVal);
    }

    // Long press for accents
    if (this.accentsMap[keyVal.toLowerCase()]) {
      this.longPressTimer = setTimeout(() => {
        this.showAccentMenu(keyBtn, keyVal.toLowerCase());
      }, 400);
    }

    // Execute Key Action
    this.processKeyInput(keyVal);
  }

  handleKeyPressEnd(keyBtn) {
    keyBtn.classList.remove('pressed');
    if (this.longPressTimer) {
      clearTimeout(this.longPressTimer);
      this.longPressTimer = null;
    }
    this.removeKeyPopup();
  }

  showKeyPopup(keyBtn, char) {
    this.removeKeyPopup();
    const popup = document.createElement('div');
    popup.className = 'key-popup-overlay';
    popup.textContent = (this.shiftState > 0) ? char.toUpperCase() : char;
    keyBtn.appendChild(popup);
  }

  removeKeyPopup() {
    const existing = this.container.querySelector('.key-popup-overlay');
    if (existing) existing.remove();
    const accentMenu = this.container.querySelector('.accent-menu');
    if (accentMenu) accentMenu.remove();
  }

  showAccentMenu(keyBtn, char) {
    this.removeKeyPopup();
    const accents = this.accentsMap[char] || [];
    if (accents.length === 0) return;

    const menu = document.createElement('div');
    menu.className = 'accent-menu';

    accents.forEach((accChar) => {
      const opt = document.createElement('span');
      opt.className = 'accent-opt';
      opt.textContent = (this.shiftState > 0) ? accChar.toUpperCase() : accChar;
      opt.onclick = (e) => {
        e.stopPropagation();
        this.insertText(opt.textContent);
        menu.remove();
      };
      menu.appendChild(opt);
    });

    keyBtn.appendChild(menu);
  }

  processKeyInput(keyVal) {
    if (keyVal === 'SHIFT') {
      this.shiftState = (this.shiftState + 1) % 3;
      this.render();
      return;
    }

    if (keyVal === 'BACKSPACE') {
      this.deleteText();
      return;
    }

    if (keyVal === 'SPACE') {
      this.insertText(' ');
      return;
    }

    if (keyVal === 'ENTER') {
      this.insertText('\n');
      return;
    }

    if (keyVal === '?123') {
      this.layoutMode = 'symbols';
      this.render();
      return;
    }

    if (keyVal === '=<') {
      this.layoutMode = 'symbols_extra';
      this.render();
      return;
    }

    if (keyVal === 'ABC') {
      this.layoutMode = 'alpha';
      this.render();
      return;
    }

    if (keyVal === 'EMOJI') {
      window.dispatchEvent(new CustomEvent('toggle-drawer', { detail: { type: 'emoji' } }));
      return;
    }

    if (keyVal === 'CLIPBOARD') {
      window.dispatchEvent(new CustomEvent('toggle-drawer', { detail: { type: 'clipboard' } }));
      return;
    }

    if (keyVal === 'VOCAL') {
      this.simulateVoiceInput();
      return;
    }

    // Single character insertion
    let charToInsert = keyVal;
    if (keyVal.length === 1 && this.layoutMode === 'alpha') {
      charToInsert = (this.shiftState > 0) ? keyVal.toUpperCase() : keyVal.toLowerCase();
      // If shifted once, revert shift to 0
      if (this.shiftState === 1) {
        this.shiftState = 0;
        this.render();
      }
    }

    this.insertText(charToInsert);
  }

  insertText(str) {
    const start = this.textarea.selectionStart;
    const end = this.textarea.selectionEnd;
    const val = this.textarea.value;

    this.textarea.value = val.substring(0, start) + str + val.substring(end);
    this.textarea.selectionStart = this.textarea.selectionEnd = start + str.length;
    this.textarea.focus();

    this.updateSuggestions();
    this.onTextChange();
  }

  deleteText() {
    const start = this.textarea.selectionStart;
    const end = this.textarea.selectionEnd;
    const val = this.textarea.value;

    if (start !== end) {
      this.textarea.value = val.substring(0, start) + val.substring(end);
      this.textarea.selectionStart = this.textarea.selectionEnd = start;
    } else if (start > 0) {
      this.textarea.value = val.substring(0, start - 1) + val.substring(start);
      this.textarea.selectionStart = this.textarea.selectionEnd = start - 1;
    }

    this.textarea.focus();
    this.updateSuggestions();
    this.onTextChange();
  }

  updateSuggestions() {
    if (!this.suggestionsEnabled) return;

    const text = this.textarea.value;
    const cursor = this.textarea.selectionStart;
    const wordBeforeCursor = text.substring(0, cursor).split(/\s+/).pop();

    const suggestions = getSuggestions(wordBeforeCursor);
    const strip = document.getElementById('suggestion-strip');
    if (!strip) return;

    strip.innerHTML = '';
    suggestions.forEach((sugWord, idx) => {
      const item = document.createElement('div');
      item.className = `suggestion-item ${idx === 0 ? 'active' : ''}`;
      item.textContent = sugWord;
      item.onclick = () => {
        this.replaceWordBeforeCursor(wordBeforeCursor, sugWord + ' ');
      };
      strip.appendChild(item);
    });
  }

  replaceWordBeforeCursor(oldWord, newWord) {
    const text = this.textarea.value;
    const cursor = this.textarea.selectionStart;
    const lastWordPos = text.substring(0, cursor).lastIndexOf(oldWord);

    if (lastWordPos !== -1) {
      const newText = text.substring(0, lastWordPos) + newWord + text.substring(cursor);
      this.textarea.value = newText;
      const newCursor = lastWordPos + newWord.length;
      this.textarea.selectionStart = this.textarea.selectionEnd = newCursor;
      this.textarea.focus();
      this.updateSuggestions();
      this.onTextChange();
    }
  }

  simulateVoiceInput() {
    const phrases = [
      "DigitBoard teclado virtual ativado por voz com sucesso.",
      "Testando entrada por ditado no teclado Android Neumorphism.",
      "Tecnologia CI/CD integrando build de APK no GitHub."
    ];
    const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];
    this.insertText(' ' + randomPhrase + ' ');
  }
}
