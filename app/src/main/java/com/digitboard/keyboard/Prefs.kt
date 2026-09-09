package com.digitboard.keyboard

import android.content.Context

object Prefs {
    private const val NAME = "digitboard_prefs"
    const val KEY_THEME = "theme"
    const val KEY_HAPTIC = "haptic"
    const val KEY_SOUND = "sound"
    const val KEY_NUMBER_ROW = "number_row"
    const val KEY_SUGGESTIONS = "suggestions"
    const val KEY_CLIPBOARD = "clipboard_history"

    fun get(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun theme(context: Context) = get(context).getString(KEY_THEME, "neumorphism") ?: "neumorphism"
    fun haptic(context: Context) = get(context).getBoolean(KEY_HAPTIC, true)
    fun sound(context: Context) = get(context).getBoolean(KEY_SOUND, true)
    fun numberRow(context: Context) = get(context).getBoolean(KEY_NUMBER_ROW, true)
    fun suggestions(context: Context) = get(context).getBoolean(KEY_SUGGESTIONS, true)

    fun clipboard(context: Context): MutableList<String> {
        val raw = get(context).getString(KEY_CLIPBOARD, "") ?: ""
        if (raw.isBlank()) {
            return mutableListOf(
                "DigitBoard - O melhor teclado Neumorphism!",
                "CI/CD com build APK automático via GitHub Actions",
                "https://github.com/HOWCKs/DigitBoard"
            )
        }
        return raw.split("\n\u001e\n").filter { it.isNotBlank() }.toMutableList()
    }

    fun addClipboard(context: Context, text: String) {
        if (text.isBlank()) return
        val list = clipboard(context)
        list.remove(text)
        list.add(0, text)
        while (list.size > 20) list.removeAt(list.lastIndex)
        get(context).edit().putString(KEY_CLIPBOARD, list.joinToString("\n\u001e\n")).apply()
    }
}
