package com.example.myapplication.imagia_app

import ServerUtils
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {

    private val menuViewModel: MenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.barra_navegacion)
        val menu = bottomNavigationView.menu

        val fileManager = FileManager(this)
        val data = fileManager.loadFromFile()
        if (data.isNotEmpty()) {
            menuViewModel.unlockMenu()
            ServerUtils.apiKey = data
        }

        menuViewModel.isMenuUnlocked.observe(this) { isUnlocked ->
            if (isUnlocked) {
                menu.findItem(R.id.navigation_camera).isEnabled = true
                menu.findItem(R.id.navigation_history).isEnabled = true
            } else {
                menu.findItem(R.id.navigation_camera).isEnabled = true
                menu.findItem(R.id.navigation_history).isEnabled = true
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_camera -> {
                    if (menuViewModel.isMenuUnlocked.value == true) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contenedor_tabs, CameraTab())
                            .commit()
                        true
                    } else {
                        Log.d("MenuStatus", "Menu bloqueado")
                        Toast.makeText(this, "Debes iniciar sesión para acceder.", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.navigation_history -> {
                    if (menuViewModel.isMenuUnlocked.value == true) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contenedor_tabs, HistoryTab(), "HISTORY_TAB")
                            .commit()
                        true
                    } else {
                        Log.d("MenuStatus", "Menu bloqueado")
                        Toast.makeText(this, "Debes iniciar sesión para acceder.", Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.navigation_user -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedor_tabs, UserTab())
                        .commit()
                    true
                }
                else -> false
            }
        }

        if (ServerUtils.apiKey.isNotEmpty()) {
            bottomNavigationView.selectedItemId = R.id.navigation_camera
        } else if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_user
        }
    }
}


