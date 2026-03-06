package de.robnice.philipstvcontrol.data.platform

import android.os.Build

object Platform {
    val isEmulator: Boolean by lazy {
        val fp = Build.FINGERPRINT.orEmpty()
        val model = Build.MODEL.orEmpty()
        val manuf = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val device = Build.DEVICE.orEmpty()
        val product = Build.PRODUCT.orEmpty()
        val hardware = Build.HARDWARE.orEmpty()
        val board = Build.BOARD.orEmpty()

        fp.startsWith("generic") ||
                fp.startsWith("unknown") ||
                brand.startsWith("generic") ||
                device.startsWith("generic") ||
                model.contains("google_sdk", ignoreCase = true) ||
                model.contains("Emulator", ignoreCase = true) ||
                model.contains("Android SDK built for", ignoreCase = true) ||
                manuf.contains("Genymotion", ignoreCase = true) ||
                product.contains("sdk", ignoreCase = true) ||
                product.contains("sdk_gwear", ignoreCase = true) ||
                product.contains("emulator", ignoreCase = true) ||
                product.contains("simulator", ignoreCase = true) ||
                product.contains("wear", ignoreCase = true) ||
                hardware.contains("goldfish", ignoreCase = true) ||
                hardware.contains("ranchu", ignoreCase = true) ||
                board.contains("goldfish", ignoreCase = true) ||
                board.contains("ranchu", ignoreCase = true)
    }
}