package com.example.myapplication.imagia_app

import android.content.Context
import java.io.File

class FileManager(private val context: Context) {
    private val filename = "data.txt"

    fun saveToFile(data: String) {
        try {
            context.openFileOutput(filename, Context.MODE_PRIVATE).use { fos ->
                fos.write(data.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFromFile(): String {
        return try {
            context.openFileInput(filename).bufferedReader().use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun deleteFile(): Boolean {
        return try {
            val file = File(context.filesDir, filename)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}