import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ServerUtils {
    companion object {

        fun postImageAnalysis(
            prompt: String,
            images: List<String> = emptyList(),
            onSuccess: (String) -> Unit,
            onFailure: (String) -> Unit
        ) {
            val client = OkHttpClient()

            val payload = JSONObject().apply {
                put("prompt", prompt)
                put("images", images)
                put("stream", false)
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("https://imagia4.ieti.site/api/analitzar-imatge")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            onFailure("Unexpected response: ${response.code}")
                        } else {
                            val responseBody = response.body?.string() ?: ""
                            onSuccess(responseBody)
                        }
                    }
                }
            })
        }
    }
}