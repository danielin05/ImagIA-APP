package com.example.myapplication.imagia_app

import TTSUtils
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.imagia_app.databinding.LayoutCamaraBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class CameraTab : Fragment(), SensorEventListener {

    private var _binding: LayoutCamaraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private var lastTapTime: Long = 0
    private var tapCount = 0
    private lateinit var ttsUtils: TTSUtils
    private var currentQuota: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutCamaraBinding.inflate(inflater, container, false)
        ttsUtils = TTSUtils(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Error al iniciar la cámara", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Tomar foto con camara x y guardarla en la galeria
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Error al capturar la foto: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Error al guardar la foto.", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val imageUri = output.savedUri
                    if (imageUri != null) {
                        Log.d(TAG, "Foto guardada: $imageUri")

                        val bitmap = ImageUtils.resizeImage(MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver,
                            imageUri
                        ))
                        val base64Image = ImageUtils.imageToBase64(bitmap)
                        // Stop the current tts
                        ttsUtils.stop()
                        // Post image analysis to the server
                        Toast.makeText(requireContext(), "Foto enviada al servidor.", Toast.LENGTH_SHORT).show()
                        ServerUtils.postImageAnalysis(
                            images = listOf<String>(base64Image),
                            onSuccess = { response ->
                                Log.d(TAG, "Image analysis successful: $response")
                                try {
                                    val jsonResponse = JSONObject(response)
                                    val dataObject = jsonResponse.getJSONObject("data")
                                    val description = dataObject.getString("description")
                                    val quota = dataObject.getString("currentQuota")
                                    printQuota(quota)

                                    Log.d(TAG, "Extracted description: $description")
                                    saveImageHistory(imageUri.toString(), description)

                                    ttsUtils.speak(description)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing JSON response: ${e.message}")
                                    ttsUtils.speak("Error processing analysis results")
                                    // Alternatively, call onFailure here if you want to propagate the error
                                }
                            },
                            onFailure = { errorMessage ->
                                Log.e(TAG, "Image analysis failed: $errorMessage")

                                requireActivity().runOnUiThread {
                                    try {
                                        val message = errorMessage.getString("message")
                                        Log.e(TAG,  message)
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    } else {
                        Log.e(TAG, "Error: URI de la imagen es nulo.")
                    }
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función que detecta cuando se mueve el sensor y toma foto
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val x = abs(event.values[0])
            val y = abs(event.values[1])
            val z = abs(event.values[2])
            val currentTime = System.currentTimeMillis()

            // Diferenciar entre los golpes y el movimiento del movil
            val tapThreshold = 1.5 // Golpe
            val movementThreshold = 1.0 // Movimiento del movil

            if (z > tapThreshold && x < movementThreshold && y < movementThreshold) {
                if (currentTime - lastTapTime < 2000) {
                    tapCount++
                    if (tapCount == 2) {
                        takePhoto()
                        tapCount = 0
                    }
                } else {
                    tapCount = 1
                }
                lastTapTime = currentTime
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
        sensorManager.unregisterListener(this)
        ttsUtils.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsUtils.shutdown()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun saveImageHistory(imageUri: String, description: String) {
        val sharedPreferences = requireContext().getSharedPreferences("image_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val jsonString = sharedPreferences.getString("image_list", null)
        val imageHistory = mutableListOf<Pair<String, String>>()

        if (!jsonString.isNullOrEmpty()) {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val uri = obj.getString("imageUri")
                val desc = obj.getString("description")
                imageHistory.add(Pair(uri, desc))
            }
        }

        imageHistory.add(Pair(imageUri, description))

        val jsonArray = JSONArray()
        imageHistory.forEach { (uri, desc) ->
            val obj = JSONObject().apply {
                put("imageUri", uri)
                put("description", desc)
            }
            jsonArray.put(obj)
        }

        editor.putString("image_list", jsonArray.toString())
        editor.apply()
    }


    private fun printQuota(quota: String) {
        currentQuota = quota.toIntOrNull() ?: 0

        requireActivity().runOnUiThread {
            if (currentQuota > 0) {
                Toast.makeText(requireContext(), "Quota disponible: $quota", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Quota agotada. No puedes tomar más fotos.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA,
        ).apply {
            // Add WRITE_EXTERNAL_STORAGE only for devices running Android 9 (Pie) or lower
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}