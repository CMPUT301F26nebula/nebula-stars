
package com.example.stars

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.nio.charset.StandardCharsets

/**
 * generates qr code images containing links to campsite details.
 *
 * each campsites unique id is encoded in a stable uri so that
 * scanning the qr code can identify the correct campsite.
 */
class QRCodeGenerator {

    /**
     * creates the qr code payload for a campsite.
     *
     * @param campsiteId unique identifier of the campsite
     * @return uri containing the campsite identifier
     * @throws IllegalArgumentException if the identifier is blank
     */
    fun createPayload(campsiteId: String): String {
        require(campsiteId.isNotBlank()) {
            "Campsite ID cannot be blank"
        }
        return "stars://campsite/${java.net.URLEncoder.encode(campsiteId, "UTF-8")}"
    }

    /**
     * generates a square qr code bitmap for a campsite.
     *
     * @param campsiteId unique identifier of the campsite
     * @param size width and height of the resulting image in pixels
     * @return bitmap containing the generated qr code
     */
    fun generate(campsiteId: String, size: Int = 512): Bitmap {
        require(size > 0) { "QR code size must be positive" }

        val payload = createPayload(campsiteId)

        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to StandardCharsets.UTF_8.name(),
            EncodeHintType.MARGIN to 2
        )

        val matrix = MultiFormatWriter().encode(
            payload,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val pixels = IntArray(size * size)

        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] =
                    if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }

        return Bitmap.createBitmap(
            pixels,
            size,
            size,
            Bitmap.Config.ARGB_8888
        )
    }
}
