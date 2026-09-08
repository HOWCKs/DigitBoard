import { KeyboardEngine } from './keyboard.js';

document.addEventListener('DOMContentLoaded', () => {
  const container = document.getElementById('keyboard-container');
  const textarea = document.getElementById('typing-textarea');
  const charCount = document.getElementById('char-count');
  const wordCount = document.getElementById('word-count');
  
  // Initialize Keyboard Engine
  const kb = new KeyboardEngine({
    container,
    textarea,
    onTextChange: updateStats
  });

  function updateStats() {
    const text = textarea.value;
    charCount.textContent = text.length;
    const words = text.trim() ? text.trim().split(/\s+/).length : 0;
    wordCount.textContent = words;
  }

  // Textarea listeners
  textarea.addEventListener('input', updateStats);
  textarea.addEventListener('keyup', () => kb.updateSuggestions());

  // Clear & Copy buttons
  document.getElementById('btn-clear-text').addEventListener('click', () => {
    textarea.value = '';
    updateStats();
    kb.updateSuggestions();
  });

  document.getElementById('btn-copy-text').addEventListener('click', () => {
    if (textarea.value) {
      navigator.clipboard.writeText(textarea.value);
      alert('Texto copiado para a área de transferência!');
    }
  });

  // Download APK Button
  document.getElementById('btn-github-apk').addEventListener('click', () => {
    window.open('https://github.com/HOWCKs/DigitBoard/releases', '_blank');
  });

  // Theme Switcher Buttons
  const themeBtns = document.querySelectorAll('.theme-option');
  themeBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      themeBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const theme = btn.getAttribute('data-theme');
      kb.setTheme(theme);
    });
  });

  // Setting Toggles
  document.getElementById('toggle-number-row').addEventListener('change', (e) => {
    kb.showNumberRow = e.target.checked;
    kb.render();
  });

  document.getElementById('toggle-suggestions').addEventListener('change', (e) => {
    kb.suggestionsEnabled = e.target.checked;
    const strip = document.getElementById('suggestion-strip');
    strip.style.display = e.target.checked ? 'flex' : 'none';
  });

  document.getElementById('toggle-haptic').addEventListener('change', (e) => {
    kb.hapticEnabled = e.target.checked;
  });

  document.getElementById('toggle-sound').addEventListener('change', (e) => {
    kb.soundEnabled = e.target.checked;
  });

  document.getElementById('toggle-popup').addEventListener('change', (e) => {
    kb.popupEnabled = e.target.checked;
  });

  // Drawer Panel Manager (Emoji, Clipboard, TextEdit)
  const drawerPanel = document.getElementById('drawer-panel');
  let currentDrawer = null;

  function toggleDrawer(type) {
    if (currentDrawer === type) {
      drawerPanel.classList.add('hidden');
      currentDrawer = null;
      return;
    }

    currentDrawer = type;
    drawerPanel.classList.remove('hidden');
    drawerPanel.innerHTML = '';

    if (type === 'emoji') {
      renderEmojiDrawer();
    } else if (type === 'clipboard') {
      renderClipboardDrawer();
    } else if (type === 'textedit') {
      renderTextEditDrawer();
    }
  }

  // Toolbar Listeners
  document.getElementById('tb-emoji').addEventListener('click', () => toggleDrawer('emoji'));
  document.getElementById('tb-clipboard').addEventListener('click', () => toggleDrawer('clipboard'));
  document.getElementById('tb-textedit').addEventListener('click', () => toggleDrawer('textedit'));
  document.getElementById('tb-mic').addEventListener('click', () => kb.simulateVoiceInput());
  document.getElementById('tb-settings').addEventListener('click', () => {
    document.querySelector('.right-panel').scrollIntoView({ behavior: 'smooth' });
  });

  window.addEventListener('toggle-drawer', (e) => toggleDrawer(e.detail.type));

  // Render Emoji Drawer
  function renderEmojiDrawer() {
    const emojis = [
      "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
      "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
      "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
      "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
      "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬",
      "👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✌️", "🤟",
      "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "💪", "🦾",
      "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
      "🔥", "✨", "🌟", "⚡", "💥", "🎉", "🎊", "🚀", "📱", "💻"
    ];

    const grid = document.createElement('div');
    grid.className = 'emoji-grid';

    emojis.forEach(emo => {
      const btn = document.createElement('button');
      btn.className = 'emoji-btn';
      btn.textContent = emo;
      btn.onclick = () => kb.insertText(emo);
      grid.appendChild(btn);
    });

    drawerPanel.appendChild(grid);
  }

  // Render Clipboard Drawer
  function renderClipboardDrawer() {
    const list = document.createElement('div');
    list.className = 'clipboard-list';

    kb.clipboardHistory.forEach(text => {
      const item = document.createElement('div');
      item.className = 'clip-item';
      item.innerHTML = `<span>${text}</span> <button class="neu-button small">Colar</button>`;
      item.onclick = () => kb.insertText(text);
      list.appendChild(item);
    });

    drawerPanel.appendChild(list);
  }

  // Render Text Precision Edit Drawer
  function renderTextEditDrawer() {
    const pad = document.createElement('div');
    pad.className = 'textedit-pad';

    const buttons = [
      { label: 'Select All', action: () => textarea.select() },
      { label: '▲', action: () => moveCursor('up') },
      { label: 'Copiar', action: () => document.execCommand('copy') },
      { label: '◄', action: () => moveCursor('left') },
      { label: '▼', action: () => moveCursor('down') },
      { label: '►', action: () => moveCursor('right') },
      { label: 'Início', action: () => moveCursor('home') },
      { label: 'Colar', action: () => navigator.clipboard.readText().then(t => kb.insertText(t)) },
      { label: 'Fim', action: () => moveCursor('end') }
    ];

    buttons.forEach(btnDef => {
      const b = document.createElement('button');
      b.className = 'neu-button small';
      b.textContent = btnDef.label;
      b.onclick = btnDef.action;
      pad.appendChild(b);
    });

    drawerPanel.appendChild(pad);
  }

  function moveCursor(dir) {
    const pos = textarea.selectionStart;
    const len = textarea.value.length;

    if (dir === 'left' && pos > 0) textarea.selectionStart = textarea.selectionEnd = pos - 1;
    if (dir === 'right' && pos < len) textarea.selectionStart = textarea.selectionEnd = pos + 1;
    if (dir === 'home') textarea.selectionStart = textarea.selectionEnd = 0;
    if (dir === 'end') textarea.selectionStart = textarea.selectionEnd = len;
    textarea.focus();
  }
});
