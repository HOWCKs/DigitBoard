package com.digitboard.keyboard

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import com.digitboard.keyboard.service.AudioHapticFeedback
import com.digitboard.keyboard.service.SuggestionEngine

class DigitBoardIME : InputMethodService() {

    private lateinit var feedback: AudioHapticFeedback
    private var shiftState = 0
    private var layoutMode = "alpha"
    private var overlayMode: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var longPress: Runnable? = null
    private var rootView: View? = null
    private var keysContainer: LinearLayout? = null
    private var suggestionContainer: LinearLayout? = null
    private var overlayContainer: LinearLayout? = null

    private val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val alphaRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"),
        listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BACKSPACE"),
        listOf("?123", "EMOJI", "VOCAL", "SPACE", ".", "ENTER")
    )
    private val symbolsRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        listOf("=<", "*", "\"", "'", ":", ";", "!", "?", "BACKSPACE"),
        listOf("ABC", "EMOJI", "CLIPBOARD", "SPACE", ",", "ENTER")
    )
    private val symbolsExtraRows = listOf(
        listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"),
        listOf("£", "¥", "€", "¢", "^", "°", "=", "{", "}", "\\"),
        listOf("?123", "%", "©", "®", "™", "✓", "[", "]", "BACKSPACE"),
        listOf("ABC", "EMOJI", "EDIT", "SPACE", "...", "ENTER")
    )
    private val accents = mapOf(
        "a" to listOf("á", "à", "ã", "â", "ä"),
        "e" to listOf("é", "è", "ê", "ë"),
        "i" to listOf("í", "ì", "î", "ï"),
        "o" to listOf("ó", "ò", "õ", "ô", "ö"),
        "u" to listOf("ú", "ù", "û", "ü"),
        "c" to listOf("ç"),
        "n" to listOf("ñ")
    )
    private val emojis = listOf(
        "😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎", "🤔", "😅",
        "😢", "😭", "😡", "👍", "👎", "👏", "🙏", "🔥", "❤️", "✨",
        "🎉", "🚀", "📱", "⌨️", "✅", "⭐", "💡", "🇧🇷", "👋", "🤝"
    )

    override fun onCreate() {
        super.onCreate()
        feedback = AudioHapticFeedback(this)
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        rootView = view
        keysContainer = view.findViewById(R.id.keys_container)
        suggestionContainer = view.findViewById(R.id.suggestion_container)
        applyTheme(view)
        renderKeyboard()
        updateSuggestions()
        return view
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        applyTheme(rootView)
        renderKeyboard()
        updateSuggestions()
    }

    private fun palette(): Triple<Int, Int, Int> {
        return when (Prefs.theme(this)) {
            "amoled" -> Triple(Color.parseColor("#000000"), Color.parseColor("#FFFFFF"), Color.parseColor("#4d6bfe"))
            "light" -> Triple(Color.parseColor("#F8F9FA"), Color.parseColor("#212529"), Color.parseColor("#4d6bfe"))
            "rgb" -> Triple(Color.parseColor("#0b0c10"), Color.parseColor("#66fcf1"), Color.parseColor("#ff007f"))
            else -> Triple(Color.parseColor("#e0e5ec"), Color.parseColor("#2d3436"), Color.parseColor("#4d6bfe"))
        }
    }

    private fun applyTheme(view: View?) {
        val (bg, _, _) = palette()
        view?.setBackgroundColor(bg)
    }

    private fun currentRows(): List<List<String>> {
        return when (layoutMode) {
            "symbols" -> symbolsRows
            "symbols_extra" -> symbolsExtraRows
            else -> {
                val rows = mutableListOf<List<String>>()
                if (Prefs.numberRow(this)) rows.add(numberRow)
                rows.addAll(alphaRows)
                rows
            }
        }
    }

    private fun renderKeyboard() {
        val container = keysContainer ?: return
        container.removeAllViews()
        overlayMode = null
        val (_, textColor, _) = palette()

        currentRows().forEach { rowKeys ->
            val rowView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            rowKeys.forEach { keyStr ->
                val weight = if (keyStr == "SPACE") 3.2f else 1f
                val btn = Button(this).apply {
                    text = displayLabel(keyStr)
                    textSize = if (keyStr.length == 1) 16f else 13f
                    setTextColor(textColor)
                    setBackgroundResource(R.drawable.bg_neumorphic_button)
                    isAllCaps = false
                    val lp = LinearLayout.LayoutParams(0, dp(46), weight).apply {
                        setMargins(dp(2), dp(2), dp(2), dp(2))
                    }
                    layoutParams = lp
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                clickFx()
                                longPress?.let { handler.removeCallbacks(it) }
                                val accentsFor = accents[keyStr.lowercase()]
                                if (accentsFor != null) {
                                    val r = Runnable { showAccentPopup(v, keyStr, accentsFor) }
                                    longPress = r
                                    handler.postDelayed(r, 400)
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                longPress?.let { handler.removeCallbacks(it) }
                                if (event.action == MotionEvent.ACTION_UP) handleKey(keyStr)
                            }
                        }
                        true
                    }
                }
                rowView.addView(btn)
            }
            container.addView(rowView)
        }
    }

    private fun displayLabel(key: String): String {
        return when (key) {
            "SHIFT" -> if (shiftState == 2) "⇪" else "⇧"
            "BACKSPACE" -> "⌫"
            "SPACE" -> "espaço"
            "ENTER" -> "↵"
            "EMOJI" -> "😊"
            "VOCAL" -> "🎙️"
            "CLIPBOARD" -> "📋"
            "EDIT" -> "✥"
            else -> {
                if (layoutMode == "alpha" && key.length == 1 && key[0].isLetter()) {
                    if (shiftState > 0) key.uppercase() else key.lowercase()
                } else key
            }
        }
    }

    private fun clickFx() {
        if (Prefs.sound(this)) feedback.playClickSound()
        if (Prefs.haptic(this)) feedback.triggerVibration()
    }

    private fun handleKey(keyStr: String) {
        val ic = currentInputConnection ?: return
        when (keyStr) {
            "SHIFT" -> {
                shiftState = (shiftState + 1) % 3
                renderKeyboard()
            }
            "BACKSPACE" -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            "SPACE" -> {
                ic.commitText(" ", 1)
                updateSuggestions()
            }
            "ENTER" -> sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            "?123" -> {
                layoutMode = "symbols"
                renderKeyboard()
            }
            "=<" -> {
                layoutMode = "symbols_extra"
                renderKeyboard()
            }
            "ABC" -> {
                layoutMode = "alpha"
                renderKeyboard()
            }
            "EMOJI" -> showOverlay("emoji")
            "CLIPBOARD" -> showOverlay("clipboard")
            "EDIT" -> showOverlay("edit")
            "VOCAL" -> startVoice()
            else -> {
                var text = keyStr
                if (layoutMode == "alpha" && keyStr.length == 1 && keyStr[0].isLetter()) {
                    text = if (shiftState > 0) keyStr.uppercase() else keyStr.lowercase()
                    if (shiftState == 1) {
                        shiftState = 0
                        renderKeyboard()
                    }
                }
                ic.commitText(text, 1)
                updateSuggestions()
            }
        }
    }

    private fun startVoice() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (_: Exception) {
            currentInputConnection?.commitText(" DigitBoard ditado ativado. ", 1)
        }
    }

    private fun showAccentPopup(anchor: View, key: String, options: List<String>) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val popup = PopupWindow(row, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        options.forEach { opt ->
            val label = if (shiftState > 0) opt.uppercase() else opt
            val b = Button(this).apply {
                text = label
                setOnClickListener {
                    currentInputConnection?.commitText(label, 1)
                    popup.dismiss()
                    updateSuggestions()
                }
            }
            row.addView(b)
        }
        popup.showAsDropDown(anchor, 0, -anchor.height * 2)
    }

    private fun showOverlay(type: String) {
        val container = keysContainer ?: return
        if (overlayMode == type) {
            renderKeyboard()
            return
        }
        overlayMode = type
        container.removeAllViews()
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        when (type) {
            "emoji" -> {
                emojis.chunked(8).forEach { chunk ->
                    val row = LinearLayout(this)
                    chunk.forEach { e ->
                        row.addView(chip(e, 1f) { currentInputConnection?.commitText(e, 1) })
                    }
                    col.addView(row)
                }
            }
            "clipboard" -> {
                Prefs.clipboard(this).forEach { item ->
                    col.addView(chip(item.take(48), 1f) {
                        currentInputConnection?.commitText(item, 1)
                    })
                }
            }
            "edit" -> {
                val actions = listOf(
                    "◀" to { sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT) },
                    "▶" to { sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT) },
                    "▲" to { sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP) },
                    "▼" to { sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN) },
                    "Início" to { sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME) },
                    "Fim" to { sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_END) },
                    "Sel. tudo" to { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) },
                    "Copiar" to {
                        val extracted = currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString()
                        if (!extracted.isNullOrBlank()) Prefs.addClipboard(this, extracted)
                        currentInputConnection?.performContextMenuAction(android.R.id.copy)
                    },
                    "Colar" to { currentInputConnection?.performContextMenuAction(android.R.id.paste) }
                )
                actions.chunked(3).forEach { chunk ->
                    val row = LinearLayout(this)
                    chunk.forEach { (label, action) ->
                        row.addView(chip(label, 1f) { action() })
                    }
                    col.addView(row)
                }
            }
        }
        col.addView(chip("Fechar painel", 1f) { renderKeyboard() })
        scroll.addView(col)
        container.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(220)))
    }

    private fun chip(label: String, weight: Float, onClick: () -> Unit): Button {
        val (_, textColor, _) = palette()
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(textColor)
            setBackgroundResource(R.drawable.bg_neumorphic_button)
            layoutParams = LinearLayout.LayoutParams(0, dp(44), weight).apply {
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
            setOnClickListener {
                clickFx()
                onClick()
            }
        }
    }

    private fun updateSuggestions() {
        val container = suggestionContainer ?: return
        container.removeAllViews()
        if (!Prefs.suggestions(this)) return
        val before = currentInputConnection?.getTextBeforeCursor(40, 0)?.toString() ?: ""
        val word = before.split(Regex("\\s+")).lastOrNull() ?: ""
        SuggestionEngine.getSuggestions(word).forEach { s ->
            val tv = TextView(this).apply {
                text = s
                textSize = 14f
                setPadding(dp(12), dp(6), dp(12), dp(6))
                setTextColor(Color.parseColor("#4d6bfe"))
                setBackgroundResource(R.drawable.bg_neumorphic_button)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, dp(8), 0) }
                layoutParams = lp
                setOnClickListener {
                    val ic = currentInputConnection ?: return@setOnClickListener
                    if (word.isNotEmpty()) {
                        ic.deleteSurroundingText(word.length, 0)
                    }
                    ic.commitText("$s ", 1)
                    updateSuggestions()
                }
            }
            container.addView(tv)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
