package com.example.mycustomappelectronicsinventory


import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddComponentActivity : AppCompatActivity() {
    // Initializing UI components
    private lateinit var componentId: EditText
    private lateinit var componentName: EditText
    private lateinit var componentCategory: Spinner
    private lateinit var componentType: Spinner
    private lateinit var componentValue: EditText
    private lateinit var componentUnit: Spinner
    private lateinit var componentQuantity: EditText
    private lateinit var componentLocation: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_component)

        componentId = findViewById(R.id.componentId)
        componentName = findViewById(R.id.componentName)
        componentCategory = findViewById(R.id.componentCategory)
        componentType = findViewById(R.id.componentType)
        componentValue = findViewById(R.id.componentValue)
        componentUnit = findViewById(R.id.componentUnit)
        componentQuantity = findViewById(R.id.componentQuantity)
        componentLocation = findViewById(R.id.componentLocation)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        setupSpinners()
        // Set listeners for buttons
        btnSave.setOnClickListener {
            saveComponent()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
    // Setup dropdown lists for component categories, types, and units
    private fun setupSpinners() {
        val categories = arrayOf("Resistor", "Capacitor", "Inductor", "Transistor", "IC", "Other")
        val types = arrayOf("SMD", "Through-Hole", "Wire", "Other")
        val units = arrayOf("Ohms", "Farads", "Henries", "Volts", "Amps", "Watts", "Other")

        componentCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        componentType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        componentUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, units)
    }
    // Save or update component details in the database
    private fun saveComponent() {
        val id = componentId.text.toString()
        val name = componentName.text.toString()
        val category = componentCategory.selectedItem.toString()
        val type = componentType.selectedItem.toString()
        val value = componentValue.text.toString()
        val quantity = componentQuantity.text.toString().toIntOrNull()
        val location = componentLocation.text.toString()
        val unit = componentUnit.selectedItem.toString()
// Validate fields
        if (id.isBlank() || name.isBlank() || category.isBlank() || type.isBlank() || value.isBlank() || quantity == null || location.isBlank() || unit.isBlank()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("id", id)
            put("name", name)
            put("category", category)
            put("type", type)
            put("value", value)
            put("quantity", quantity)
            put("location", location)
            put("unit", unit)
        }
        val newComponent = Component(id, name, category, type, value, quantity, location, unit)

        val resultIntent = Intent()
        resultIntent.putExtra("newComponent", newComponent)
        setResult(RESULT_OK, resultIntent)

        db.insert("components", null, values)

        Toast.makeText(this, "Component added", Toast.LENGTH_SHORT).show()
        finish()
    }
}
