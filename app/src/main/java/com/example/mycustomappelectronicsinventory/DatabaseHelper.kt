package com.example.mycustomappelectronicsinventory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "components.db"
        const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
             CREATE TABLE components (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                type TEXT NOT NULL,
                value TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                location TEXT NOT NULL,
                unit TEXT NOT NULL
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS components")
            onCreate(db)
        }
    }
}