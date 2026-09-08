package com.digitboard.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.digitboard.keyboard.service.AudioHapticFeedback
import com.digitboard.keyboard.service.SuggestionEngine

class DigitBoardIME : InputMethodService() {

    private lateinit var feedback: AudioHapticFeedback
    private var isShifted = false
    private var isCapsLocked = false
    private var currentTheme = "neumorphism"

    override fun onCreate() {
        super.onCreate()
        feedback = AudioHapticFeedback(this)
    }

    override fun onCreateInputView(): View {
        val inflater = layoutInflater
        val mainView = inflater.inflate(R.layout.activity_settings, null) // Dynamic Inflate or Custom Layout
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#e0e5ec"))
        }

        // Suggestion Strip
        val suggestionStrip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(android.graphics.Color.parseColor("#e0e5ec"))
        }

        val suggestions = SuggestionEngine.getSuggestions("")
        suggestions.forEach { word ->
            val sugTv = TextView(this).apply {
                text = word
                textSize = 14f
                setPadding(24, 12, 24, 12)
                setTextColor(android.graphics.Color.parseColor("#4d6bfe"))
                setOnClickListener {
                    currentInputConnection?.commitText("$word ", 1)
                }
            }
            suggestionStrip.addView(sugTv)
        }
        container.addView(suggestionStrip)

        // Custom Keyboard View rendering demo layout
        val keyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 16)
        }

        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"),
            listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
            listOf("?123", "😊", "🎙️", "ESPAÇO", ".", "↵")
        )

        rows.forEach { rowKeys ->
            val rowView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = rowKeys.size.toFloat()
            }

            rowKeys.forEach { keyStr ->
                val btn = Button(this).apply {
                    text = keyStr
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#2d3436"))
                    setBackgroundResource(R.drawable.bg_neumorphic_button)
                    val lp = LinearLayout.LayoutParams(0, 130, 1f).apply {
                        setMargins(4, 4, 4, 4)
                    }
                    if (keyStr == "ESPAÇO") {
                        lp.weight = 3f
                    }
                    layoutParams = lp

                    setOnClickListener {
                        feedback.playClickSound()
                        feedback.triggerVibration()
                        handleKeyInput(keyStr)
                    }
                }
                rowView.addView(btn)
            }
            keyLayout.addView(rowView)
        }

        container.addView(keyLayout)
        return container
    }

    private fun handleKeyInput(keyStr: String) {
        val ic = currentInputConnection ?: return

        when (keyStr) {
            "⌫" -> ic.deleteSurroundingText(1, 0)
            "ESPAÇO" -> ic.commitText(" ", 1)
            "↵" -> ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            "⇧" -> {
                isShifted = !isShifted
            }
            "😊" -> ic.commitText("😊", 1)
            "🎙️" -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            else -> {
                val textToCommit = if (isShifted) keyStr.uppercase() else keyStr.lowercase()
                ic.commitText(textToCommit, 1)
                if (isShifted && !isCapsLocked) {
                    isShifted = false
                }
            }
        }
    }
}
