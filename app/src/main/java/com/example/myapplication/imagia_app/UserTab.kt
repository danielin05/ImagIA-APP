package com.example.myapplication.imagia_app

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment

class UserTab : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.layout_usuario, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val btnLogin = view.findViewById<ImageButton>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()

            when {
                email.isEmpty() || username.isEmpty() -> {
                    showToast("Todos los campos deben estar completos")
                }
                !isValidEmail(email) -> {
                    showToast("El correo electrónico no es válido")
                }
                else -> {
                    showToast("Registro completado")
                }
            }
        }

        return view
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
