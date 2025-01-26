import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream


class ImageUtils {
    companion object {
        fun imageToBase64(image: Bitmap, quality: Int = 50): String {
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val imageBytes = baos.toByteArray()
            return Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }

        fun resizeImage(image: Bitmap, maxWidth: Int = 800, maxHeight: Int = 600): Bitmap {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth / ratioBitmap).toInt()
            }

            return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        }
    }
}