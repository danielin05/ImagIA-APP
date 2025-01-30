package com.example.myapplication.imagia_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        // Iniciar barra de navegación
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.barra_navegacion)
        val menu = bottomNavigationView.menu
        menu.findItem(R.id.navigation_camera).isChecked = false
        menu.findItem(R.id.navigation_history).isChecked = false

        // Mandar al layout correspondiente cuando pulsas en la opción del menú
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_camera -> {
                    if (!item.isChecked) {
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
                    if (!item.isChecked) {
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

        // Cargar el fragmento por defecto
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_user
        }
    }
}
