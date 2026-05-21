package de.robnice.philipstvcontrol.data.tv

import java.security.MessageDigest
import java.util.Locale
import kotlin.random.Random

data class DigestHeader(
    val realm: String,
    val nonce: String,
    val qop: String? = null,
    val opaque: String? = null
) {
    companion object {
        fun parse(header: String): DigestHeader? {
            val params = header.removePrefix("Digest").trim()
                .split(",")
                .mapNotNull { part ->
                    val idx = part.indexOf('=')
                    if (idx <= 0) null else part.substring(0, idx).trim() to part.substring(idx + 1).trim().trim('"')
                }.toMap()

            val realm = params["realm"] ?: return null
            val nonce = params["nonce"] ?: return null
            return DigestHeader(
                realm = realm,
                nonce = nonce,
                qop = params["qop"],
                opaque = params["opaque"]
            )
        }
    }

    fun buildAuthorization(user: String, pass: String, method: String, uri: String): String {
        val nc = "00000001"
        val cnonce = Random.nextBytes(8).joinToString("") { "%02x".format(it) }

        val ha1 = md5("$user:$realm:$pass")
        val ha2 = md5("$method:$uri")

        val response = if (qop != null && qop.contains("auth", ignoreCase = true)) {
            md5("$ha1:$nonce:$nc:$cnonce:auth:$ha2")
        } else {
            md5("$ha1:$nonce:$ha2")
        }

        val sb = StringBuilder()
        sb.append("Digest ")
        sb.append("username=\"$user\", ")
        sb.append("realm=\"$realm\", ")
        sb.append("nonce=\"$nonce\", ")
        sb.append("uri=\"$uri\", ")
        sb.append("response=\"$response\"")

        if (qop != null && qop.contains("auth", ignoreCase = true)) {
            sb.append(", qop=auth, ")
            sb.append("nc=$nc, ")
            sb.append("cnonce=\"$cnonce\"")
        }
        if (opaque != null) {
            sb.append(", opaque=\"$opaque\"")
        }
        return sb.toString()
    }

    private fun md5(s: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(s.toByteArray(Charsets.ISO_8859_1))
        return bytes.joinToString("") { "%02x".format(it).lowercase(Locale.ROOT) }
    }
}