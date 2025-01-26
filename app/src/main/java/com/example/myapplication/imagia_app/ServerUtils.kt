import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

class ServerUtils {
    companion object {
        private val localizedPrompts = mapOf(
            "en" to "Analyze this image and describe its contents in detail",
            "es" to "Analiza esta imagen y describe su contenido en detalle",
            "ca" to "Analitza aquesta imatge i descriu-ne el contingut detalladament",
            "fr" to "Analysez cette image et décrivez son contenu en détail",
            "de" to "Analysieren Sie dieses Bild und beschreiben Sie den Inhalt im Detail"
        ).withDefault { "Analyze this image and describe its contents in detail" } // English default


        fun postImageAnalysis(
            images: List<String> = emptyList(),
            locale: String = Locale.getDefault().language,
            onSuccess: (String) -> Unit,
            onFailure: (String) -> Unit
        ) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Set the connection timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Set the read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Set the write timeout
                .build()
            val tag = "PostImageAnalysis"

            Log.d(tag, "Preparing payload for the request")
            val payload = JSONObject().apply {
                put("prompt", localizedPrompts.getValue(locale))
                put("stream", false)
                put("images", JSONArray(images))
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())
            Log.d(tag, "Payload: $payload")

            val request = Request.Builder()
                .url("https://imagia4.ieti.site/api/analitzar-imatge")
                .post(requestBody)
                .build()

            Log.d(tag, "Request prepared. Sending request to the server.")
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val errorMessage = "Network error: ${e.message}"
                    Log.e(tag, errorMessage, e)
                    onFailure(errorMessage)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val responseBody = response.body?.string() ?: ""
                        if (!response.isSuccessful) {
                            val unexpectedResponseMessage = "Unexpected response: $responseBody"
                            Log.w(tag, unexpectedResponseMessage)
                            onFailure(unexpectedResponseMessage)
                        } else {
                            Log.d(tag, "Response received successfully: $responseBody")
                            onSuccess(responseBody)
                        }
                    }
                }
            })
        }
    }
}