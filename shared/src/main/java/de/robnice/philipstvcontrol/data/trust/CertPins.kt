package de.robnice.philipstvcontrol.data.trust

import android.util.Base64
import java.security.MessageDigest
import java.security.cert.X509Certificate

object CertPins {
    fun okhttpSha256Pin(cert: X509Certificate): String {
        val pubKey = cert.publicKey.encoded
        val sha256 = MessageDigest.getInstance("SHA-256").digest(pubKey)
        val b64 = Base64.encodeToString(sha256, Base64.NO_WRAP)
        return "sha256/$b64"
    }

    fun prettyHexFingerprint(cert: X509Certificate): String {
        val der = cert.encoded
        val sha256 = MessageDigest.getInstance("SHA-256").digest(der)
        return sha256.joinToString(":") { b -> "%02X".format(b) }
    }
}