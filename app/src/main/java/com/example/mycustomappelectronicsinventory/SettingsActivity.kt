package com.example.mycustomappelectronicsinventory

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var httpServerEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        httpServerEditText = findViewById(R.id.etHttpServer)
        saveButton = findViewById(R.id.btnSave)

        // Load saved server address if exists
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        httpServerEditText.setText(sharedPreferences.getString("httpServer", ""))

        saveButton.setOnClickListener {
            val httpServer = httpServerEditText.text.toString()
            if (httpServer.isBlank()) {
                Toast.makeText(this, "Please enter a valid HTTP server address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the server address
            val editor = sharedPreferences.edit()
            editor.putString("httpServer", httpServer)
            editor.apply()

            Toast.makeText(this, "HTTP server address saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
