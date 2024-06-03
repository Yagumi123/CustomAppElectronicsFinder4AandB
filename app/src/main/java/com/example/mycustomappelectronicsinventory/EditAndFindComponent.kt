package com.example.mycustomappelectronicsinventory


import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class EditAndFindComponent : AppCompatActivity() {

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
    private lateinit var btnFind: Button
    private var componentIdValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_and_find_component)

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
        btnFind = findViewById(R.id.btnFind)

        setupSpinners()

        componentIdValue = intent.getStringExtra("componentId")

        if (componentIdValue != null) {
            // Retrieve component details from the database
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "components",
                null,
                "id = ?",
                arrayOf(componentIdValue),
                null,
                null,
                null
            )
            if (cursor.moveToFirst()) {
                componentId.setText(cursor.getString(cursor.getColumnIndexOrThrow("id")))
                componentName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                componentCategory.setSelection(getIndex(componentCategory, cursor.getString(cursor.getColumnIndexOrThrow("category"))))
                componentType.setSelection(getIndex(componentType, cursor.getString(cursor.getColumnIndexOrThrow("type"))))
                componentValue.setText(cursor.getString(cursor.getColumnIndexOrThrow("value")))
                componentQuantity.setText(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")).toString())
                componentLocation.setText(cursor.getString(cursor.getColumnIndexOrThrow("location")))
                componentUnit.setSelection(getIndex(componentUnit, cursor.getString(cursor.getColumnIndexOrThrow("unit"))))
            }
            cursor.close()
        }

        btnSave.setOnClickListener {
            saveComponent()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnFind.setOnClickListener {
            sendFindRequest(componentLocation.text.toString())
        }
    }

    private fun setupSpinners() {
        val categories = arrayOf("Resistor", "Capacitor", "Inductor", "Transistor", "IC", "Other")
        val types = arrayOf("SMD", "THT", "Other")
        val units = arrayOf("Ohms", "Farad", "Henry", "Ampere", "Volt", "Watt")

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        componentCategory.adapter = categoryAdapter

        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        componentType.adapter = typeAdapter

        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        componentUnit.adapter = unitAdapter
    }

    private fun getIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    private fun saveComponent() {
        val id = componentId.text.toString()
        val name = componentName.text.toString()
        val category = componentCategory.selectedItem.toString()
        val type = componentType.selectedItem.toString()
        val valueStr = componentValue.text.toString()
        val unit = componentUnit.selectedItem.toString()
        val quantityStr = componentQuantity.text.toString()
        val location = componentLocation.text.toString()

        if (name.isBlank() || id.isBlank() || valueStr.isBlank() || quantityStr.isBlank() || location.isBlank()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!location.matches(Regex("^[A-Za-z],[0-9]+$"))) {
            Toast.makeText(this, "Location must be in the format 'S,2'", Toast.LENGTH_SHORT).show()
            return
        }

        val value: Int
        val quantity: Int
        try {
            value = valueStr.toInt()
            quantity = quantityStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Value and Quantity must be integers", Toast.LENGTH_SHORT).show()
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

        db.update("components", values, "id = ?", arrayOf(componentIdValue))

        Toast.makeText(this, "Component updated", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun sendFindRequest(location: String) {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val httpServer = sharedPreferences.getString("httpServer", null)

        if (httpServer.isNullOrEmpty()) {
            Toast.makeText(this, "No HTTP server address set. Please set it in the settings.", Toast.LENGTH_LONG).show()
            return
        }

        val url = "$httpServer/find"
        FindTask(this).execute(url, location)
    }

    private class FindTask(private val context: Context) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            val url = params[0] ?: return "Invalid URL"
            val location = params[1] ?: return "Invalid Location"

            return try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write("location=$location")
                outputStreamWriter.flush()
                outputStreamWriter.close()

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    "Success"
                } else {
                    "Error: $responseCode"
                }
            } catch (e: Exception) {
                "Exception: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }
}
