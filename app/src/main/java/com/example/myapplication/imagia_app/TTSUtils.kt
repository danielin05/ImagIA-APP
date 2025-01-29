import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTSUtils(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA && 
                                result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            throw IllegalStateException("TTS not initialized")
        }
        
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    fun stop () {
        textToSpeech?.stop()
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
