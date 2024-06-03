package com.example.mycustomappelectronicsinventory


import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    // Initialization of UI components and variables
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComponentAdapter
    private lateinit var searchView: SearchView
    private lateinit var filterSpinner: Spinner
    private lateinit var addComponentButton: Button
    private lateinit var removeSelectedButton: Button
    private lateinit var settingsButton: ImageButton
    private val components = mutableListOf<Component>()
    private var httpServer: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Load HTTP server address from SharedPreferences
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        httpServer = sharedPreferences.getString("httpServer", null)

        // Toast messages to inform the user about the HTTP server status
        if (httpServer.isNullOrEmpty()) {
            Toast.makeText(this, "No HTTP server address set. Please set it in the settings.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Loaded HTTP server address: $httpServer", Toast.LENGTH_SHORT).show()
        }
        // Setting up RecyclerView and its adapter for displaying components
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ComponentAdapter(components) { componentId ->
            showBottomSheet(componentId)
        }
        recyclerView.adapter = adapter

        // Setup for search view and spinner for filtering
        searchView = findViewById(R.id.search_view)
        filterSpinner = findViewById(R.id.spinner_filter)
        addComponentButton = findViewById(R.id.button_add_component)
        removeSelectedButton = findViewById(R.id.button_remove_selected)
        settingsButton = findViewById(R.id.button_settings)

        // Button click listeners for adding and removing components
        addComponentButton.setOnClickListener {
            val intent = Intent(this, AddComponentActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_COMPONENT)

        }

        removeSelectedButton.setOnClickListener {
            val selectedComponent = adapter.getSelectedComponent()
            if (selectedComponent != null) {
                removeComponent(selectedComponent.id)
            } else {
                Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show()
            }
            removeSelectedComponent()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        // Method calls to set up the spinner and search functionality
        setupSpinner()
        setupSearchView()
        fetchComponents()// Initially fetch components to display
    }
    private fun addComponent(component: Component) {
        // Add the component to the database
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("id", component.id)
            put("name", component.name)
            put("category", component.category)
            put("type", component.type)
            put("value", component.value)
            put("quantity", component.quantity)
            put("location", component.location)
            put("unit", component.unit)
        }

        val newRowId = db.insert("components", null, values)


            // Add the component to the list and update the RecyclerView
            components.add(component)
            adapter.notifyDataSetChanged()


    }

    private fun removeComponent(componentId: String) {
        // Remove the component from the database
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val selection = "id = ?"
        val selectionArgs = arrayOf(componentId)
        val deletedRows = db.delete("components", selection, selectionArgs)

        // Check if the component was successfully removed from the database
        if (deletedRows > 0) {
            // Remove the component from the list and update the RecyclerView
            val iterator = components.iterator()
            while (iterator.hasNext()) {
                val component = iterator.next()
                if (component.id == componentId) {
                    iterator.remove()
                    break
                }
            }
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Failed to remove component from database", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onResume() {
        super.onResume()
        fetchComponents()
    }
    // Inflate the menu options from XML
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Handle action bar item clicks here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    // Set up spinner for filtering components by categories
    private fun setupSpinner() {
        val filterOptions = resources.getStringArray(R.array.filter_options)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                filterComponents(filterOptions[position], searchView.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    // Setup for handling search queries and filtering results
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterComponents(filterSpinner.selectedItem.toString(), query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterComponents(filterSpinner.selectedItem.toString(), newText ?: "")
                return true
            }
        })
    }

    private fun fetchComponents() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "components", // The table to query
            null, // The array of columns to return (pass null to get all)
            null, // The columns for the WHERE clause
            null, // The values for the WHERE clause
            null, // Don't group the rows
            null, // Don't filter by row groups
            null // The sort order
        )

        components.clear()
        while (cursor.moveToNext()) {
            val component = Component(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                value = cursor.getString(cursor.getColumnIndexOrThrow("value")),
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"))
            )
            components.add(component)
        }
        cursor.close()
        adapter.updateComponents(components)
    }
    // Fetch components from the database and update the RecyclerView
    // Apply filters to the component list based on the user's selections
    private fun filterComponents(filterOption: String, searchText: String) {
        val filteredComponents = components.filter { component ->
            when (filterOption) {
                "Name" -> component.name.contains(searchText, ignoreCase = true)
                "Category" -> component.category.contains(searchText, ignoreCase = true)
                "Type" -> component.type.contains(searchText, ignoreCase = true)
                else -> false
            }
        }
        adapter.updateComponents(filteredComponents)
    }
    // Show a bottom sheet with component details
    private fun showBottomSheet(componentId: String) {
        val bottomSheetFragment = BottomSheetFragment(componentId)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }
    // Handle removal of selected component
    private fun removeSelectedComponent() {
        val selectedComponent = adapter.getSelectedComponent()

        if (selectedComponent != null) {
            AlertDialog.Builder(this)
                .setTitle("Remove Component")
                .setMessage("Are you sure you want to remove ${selectedComponent.name}?")
                .setPositiveButton("Yes") { dialog, which ->
                    val dbHelper = DatabaseHelper(this)
                    val db = dbHelper.writableDatabase
                    db.delete("components", "id = ?", arrayOf(selectedComponent.id))
                    adapter.clearSelection()
                    fetchComponents() // Refresh the RecyclerView
                    Toast.makeText(this, "${selectedComponent.name} removed.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show()
        }
    }  // React to activity results, particularly from AddComponentActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_COMPONENT && resultCode == RESULT_OK) {
            // Fetch the newly added component from the AddComponentActivity
            val newComponent = data?.getParcelableExtra<Component>("newComponent")
            if (newComponent != null) {
                addComponent(newComponent)
            } else {
                Toast.makeText(this, "Failed to add component", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_ADD_COMPONENT = 1
    }
}
