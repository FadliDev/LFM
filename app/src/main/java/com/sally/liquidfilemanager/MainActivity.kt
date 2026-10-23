package com.sally.liquidfilemanager

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bridgeStatus = runCatching { NativeBridge().nativeVersion() }
            .getOrElse { "Rust bridge belum tersedia: ${it.javaClass.simpleName}" }

        val title = TextView(this).apply {
            text = "Liquid File Manager"
            textSize = 24f
        }
        val description = TextView(this).apply {
            text = "Tahap 1 • Fondasi proyek\\n\\n$bridgeStatus\\n\\nFile browser dan archive engine akan dibangun pada tahap berikutnya."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            addView(title)
            addView(description)
        })
    }
}
