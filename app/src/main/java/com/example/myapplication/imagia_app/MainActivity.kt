package com.example.myapplication.imagia_app

import android.os.Bundle
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

        menuViewModel.isMenuUnlocked.observe(this) { isUnlocked ->
            if (isUnlocked) {
                menu.findItem(R.id.navigation_camera).isEnabled = true
                menu.findItem(R.id.navigation_history).isEnabled = true
            } else {
                menu.findItem(R.id.navigation_camera).isEnabled = false
                menu.findItem(R.id.navigation_history).isEnabled = false
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_camera -> {
                    if (!menuViewModel.isMenuUnlocked.value!!) {
                        Toast.makeText(this, "Debes iniciar sesión para acceder.", Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contenedor_tabs, CameraTab())
                            .commit()
                        true
                    }
                }
                R.id.navigation_history -> {
                    if (!menuViewModel.isMenuUnlocked.value!!) {
                        Toast.makeText(this, "Debes iniciar sesión para acceder.", Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.contenedor_tabs, HistoryTab())
                            .commit()
                        true
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

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_user
        }
    }
}

