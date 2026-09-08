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

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        setContentView(R.layout.activity_settings)

        val btnEnable = findViewById<Button>(R.id.btn_enable_ime)
        val btnSelect = findViewById<Button>(R.id.btn_select_ime)
        val rgThemes = findViewById<RadioGroup>(R.id.rg_themes)

        btnEnable?.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        btnSelect?.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        rgThemes?.setOnCheckedChangeListener { _, checkedId ->
            val themeName = when (checkedId) {
                R.id.rb_theme_neu -> "Neumorphism"
                R.id.rb_theme_amoled -> "Escuro AMOLED"
                R.id.rb_theme_light -> "Claro / Branco"
                R.id.rb_theme_rgb -> "RGB Animado"
                else -> "Neumorphism"
            }
            Toast.makeText(this, "Tema selecionado: $themeName", Toast.LENGTH_SHORT).show()
        }
    }
}
