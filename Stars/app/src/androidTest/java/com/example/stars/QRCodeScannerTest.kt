
package com.example.stars

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith




@RunWith(AndroidJUnit4::class)
class QRCodeScannerTest {

    @Test
    fun validCampsiteQrCodeReturnsId() {
        val result = parseCampsiteQrCode("stars://campsite/test123")
        assertEquals("test123", result)
    }

    @Test
    fun encodedCampsiteIdReturnsDecodedId() {
        val result = parseCampsiteQrCode("stars://campsite/test%20camp")
        assertEquals("test camp", result)
    }

    @Test
    fun invalidWebsiteQrCodeReturnsNull() {
        val result = parseCampsiteQrCode("https://example.com")
        assertNull(result)
    }

    @Test
    fun emptyQrCodeReturnsNull() {
        assertNull(parseCampsiteQrCode(""))
        assertNull(parseCampsiteQrCode(null))
    }

    @Test
    fun invalidCampsitePathReturnsNull() {
        assertNull(parseCampsiteQrCode("stars://campsite/"))
        assertNull(parseCampsiteQrCode("stars://campsite/one/two"))
        assertNull(parseCampsiteQrCode("stars://other/test123"))
    }
}
