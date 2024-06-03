package com.example.mycustomappelectronicsinventory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment(private val componentId: String) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_fragment, container, false)

        val title = view.findViewById<TextView>(R.id.bottomSheetTitle)
        val details = view.findViewById<TextView>(R.id.bottomSheetDetails)
        val button = view.findViewById<Button>(R.id.bottomSheetButton)

        // Retrieve component details from database
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "components",
            null,
            "id = ?",
            arrayOf(componentId.toString()),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            title.text = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            details.text = "ID: $componentId\n" +
                    "Category: ${cursor.getString(cursor.getColumnIndexOrThrow("category"))}\n" +
                    "Type: ${cursor.getString(cursor.getColumnIndexOrThrow("type"))}\n" +
                    "Value: ${cursor.getString(cursor.getColumnIndexOrThrow("value"))}\n" +
                    "Quantity: ${cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))}\n" +
                    "Location: ${cursor.getString(cursor.getColumnIndexOrThrow("location"))}"
        }
        cursor.close()

        button.setOnClickListener {
            // Start EditAndFindComponent activity
            val intent = Intent(activity, EditAndFindComponent::class.java).apply {
                putExtra("componentId", componentId)
            }
            startActivity(intent)
        }

        return view
    }
}
