package com.example.myapplication.imagia_app

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                    val input = EditText(requireContext()).apply {
                        inputType = InputType.TYPE_CLASS_PHONE
                        maxEms = 6
                        minEms = 6
                        setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_phone,
                            0,
                            0,
                            0
                        )
                        compoundDrawablePadding = 16
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val containerPhone = FrameLayout(requireContext()).apply {
                        setPadding(50, 20, 50, 20)
                        addView(input, FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                        ))
                    }

                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Verificación de registro")
                        .setMessage("Ingresa tu número de teléfono con el código de país incluido. Te enviaremos un mensaje con un código de verificación para continuar.")
                        .setView(containerPhone)
                        .setCancelable(false)
                        .setPositiveButton("Enviar", null)
                        .setNegativeButton("Cancelar", null)
                        .create()


                    dialog.setOnShowListener {
                        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        button.setOnClickListener {
                            val phoneNumber = input.text.toString().trim()
                            if (phoneNumber.length != 9) {
                                input.error = "teléfono inválido"
                            } else {
                                // Procesar el número
                                dialog.dismiss()

                                val inputSMS = EditText(requireContext()).apply {
                                    inputType = InputType.TYPE_CLASS_NUMBER
                                    minEms = 5
                                    maxEms = 5
                                    setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_sms,
                                        0,
                                        0,
                                        0
                                    )
                                    compoundDrawablePadding = 16
                                    filters = arrayOf(InputFilter.LengthFilter(6))
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                }

                                val containerSMS = FrameLayout(requireContext()).apply {
                                    setPadding(50, 20, 50, 20)
                                    addView(inputSMS, FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        Gravity.CENTER
                                    ))
                                }

                                val dialogSMS = AlertDialog.Builder(requireContext())
                                    .setTitle("Código de verificación")
                                    .setMessage("Ingresa el código que hemos enviado al teléfono " + input.text.toString())
                                    .setView(containerSMS)
                                    .setCancelable(false)
                                    .setPositiveButton("Verificar", null)
                                    .setNegativeButton("Cancelar", null)
                                    .create()

                                dialogSMS.setOnShowListener {
                                    val verifyButton = dialogSMS.getButton(AlertDialog.BUTTON_POSITIVE)
                                    verifyButton.setOnClickListener {
                                        val smsCode = inputSMS.text.toString().trim()

                                        if (smsCode.isEmpty()) {
                                            inputSMS.error = "El código no puede estar vacío"
                                        } else {
                                            showToast("Código ingresado: $smsCode")
                                        }
                                    }
                                }

                                dialogSMS.show()
                            }
                        }
                    }

                    dialog.show()
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
