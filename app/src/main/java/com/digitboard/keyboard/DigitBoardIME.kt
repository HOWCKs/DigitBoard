package com.digitboard.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.digitboard.keyboard.service.AudioHapticFeedback
import com.digitboard.keyboard.service.SuggestionEngine

class DigitBoardIME : InputMethodService() {

    private lateinit var feedback: AudioHapticFeedback
    private var isShifted = false
    private var isCapsLocked = false

    override fun onCreate() {
        super.onCreate()
        feedback = AudioHapticFeedback(this)
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        val keysContainer = view.findViewById<LinearLayout>(R.id.keys_container)
        val suggestionContainer = view.findViewById<LinearLayout>(R.id.suggestion_container)

        if (suggestionContainer != null) {
            updateSuggestions(suggestionContainer, "")
        }

        val rows = listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"),
            listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
            listOf("?123", "😊", "🎙️", "ESPAÇO", ".", "↵")
        )

        keysContainer?.let { container ->
            rows.forEach { rowKeys ->
                val rowView = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = rowKeys.size.toFloat()
                }

                rowKeys.forEach { keyStr ->
                    val btn = Button(this).apply {
                        text = keyStr
                        textSize = 15f
                        setTextColor(android.graphics.Color.parseColor("#2d3436"))
                        setBackgroundResource(R.drawable.bg_neumorphic_button)
                        val lp = LinearLayout.LayoutParams(0, 120, 1f).apply {
                            setMargins(3, 3, 3, 3)
                        }
                        if (keyStr == "ESPAÇO") {
                            lp.weight = 3f
                        }
                        layoutParams = lp

                        setOnClickListener {
                            feedback.playClickSound()
                            feedback.triggerVibration()
                            handleKeyInput(keyStr, suggestionContainer)
                        }
                    }
                    rowView.addView(btn)
                }
                container.addView(rowView)
            }
        }

        return view
    }

    private fun updateSuggestions(container: LinearLayout?, inputWord: String) {
        if (container == null) return
        container.removeAllViews()
        val suggestions = SuggestionEngine.getSuggestions(inputWord)
        suggestions.forEach { word ->
            val sugTv = TextView(this).apply {
                text = word
                textSize = 14f
                setPadding(24, 10, 24, 10)
                setTextColor(android.graphics.Color.parseColor("#4d6bfe"))
                setBackgroundResource(R.drawable.bg_neumorphic_button)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 12, 0)
                }
                layoutParams = lp
                setOnClickListener {
                    currentInputConnection?.commitText("$word ", 1)
                }
            }
            container.addView(sugTv)
        }
    }

    private fun handleKeyInput(keyStr: String, suggestionContainer: LinearLayout?) {
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
