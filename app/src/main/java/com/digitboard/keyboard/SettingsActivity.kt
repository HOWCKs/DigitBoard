package com.digitboard.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = Prefs.get(this)

        findViewById<Button>(R.id.btn_enable_ime)?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        findViewById<Button>(R.id.btn_select_ime)?.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        val rgThemes = findViewById<RadioGroup>(R.id.rg_themes)
        when (Prefs.theme(this)) {
            "amoled" -> rgThemes?.check(R.id.rb_theme_amoled)
            "light" -> rgThemes?.check(R.id.rb_theme_light)
            "rgb" -> rgThemes?.check(R.id.rb_theme_rgb)
            else -> rgThemes?.check(R.id.rb_theme_neu)
        }
        rgThemes?.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.rb_theme_amoled -> "amoled"
                R.id.rb_theme_light -> "light"
                R.id.rb_theme_rgb -> "rgb"
                else -> "neumorphism"
            }
            prefs.edit().putString(Prefs.KEY_THEME, theme).apply()
            Toast.makeText(this, "Tema salvo: $theme", Toast.LENGTH_SHORT).show()
        }

        bindSwitch(R.id.sw_haptic, Prefs.KEY_HAPTIC, Prefs.haptic(this))
        bindSwitch(R.id.sw_sound, Prefs.KEY_SOUND, Prefs.sound(this))
        bindSwitch(R.id.sw_number_row, Prefs.KEY_NUMBER_ROW, Prefs.numberRow(this))
        bindSwitch(R.id.sw_suggestions, Prefs.KEY_SUGGESTIONS, Prefs.suggestions(this))
    }

    private fun bindSwitch(id: Int, key: String, value: Boolean) {
        findViewById<SwitchCompat>(id)?.apply {
            isChecked = value
            setOnCheckedChangeListener { _, checked ->
                Prefs.get(this@SettingsActivity).edit().putBoolean(key, checked).apply()
            }
        }
    }
}
