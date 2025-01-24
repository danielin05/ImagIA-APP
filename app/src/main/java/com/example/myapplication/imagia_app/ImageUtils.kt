import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream


class ImageUtils {
    companion object {
        fun imageToBase64(image: Bitmap): String {
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageBytes = baos.toByteArray()
            return Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }
    }
}