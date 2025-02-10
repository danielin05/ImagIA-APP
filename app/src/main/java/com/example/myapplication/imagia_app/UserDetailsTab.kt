package com.example.myapplication.imagia_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDetailsTab : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_usuario_registrado, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("UserData", android.content.Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "Nombre no disponible")
        val userEmail = sharedPreferences.getString("userEmail", "Correo no disponible")
        val userPhone = sharedPreferences.getString("userPhone", "Teléfono no disponible")

        val userNameEditText = view.findViewById<TextView>(R.id.userName)
        val userEmailEditText = view.findViewById<EditText>(R.id.userEmail)
        val userPhoneEditText = view.findViewById<EditText>(R.id.userPhone)

        userNameEditText.text = "$userName"
        userEmailEditText.setText("Correo: $userEmail")
        userPhoneEditText.setText("Teléfono: $userPhone")

        val logoutButton = view.findViewById<ImageButton>(R.id.log_out)
        logoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Sesión cerrada.", Toast.LENGTH_SHORT).show()
            val fileManager = FileManager(requireContext())
            fileManager.deleteFile()
            ServerUtils.apiKey = ""

            with(sharedPreferences.edit()) {
                clear()
                apply()
            }

            val historyTab = parentFragmentManager.findFragmentByTag("HISTORY_TAB") as? HistoryTab
            historyTab?.clearHistory()

            val menuViewModel = ViewModelProvider(requireActivity()).get(MenuViewModel::class.java)
            lifecycleScope.launch(Dispatchers.Main) {
                menuViewModel.lockMenu()
            }

            val userTab = UserTab()
            parentFragmentManager.beginTransaction()
                .replace(R.id.contenedor_tabs, userTab)
                .addToBackStack(null)
                .commit()

            parentFragmentManager.executePendingTransactions()
        }

        return view
    }
}
