package de.robnice.philipstvcontrol.data.tv

import android.util.Base64
import android.util.Log
import de.robnice.philipstvcontrol.data.net.OkHttpFactory
import de.robnice.philipstvcontrol.data.trust.DEV_TRUST_ALL
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PhilipsPairingService(
    private val okHttpFactory: OkHttpFactory
) {
    private val secretKeyBase64 =
        "ZmVay1EQVFOaZhwQ4Kv81ypLAZNczV9sG4KkseXWn1NEk6cXmPKO/MCa9sryslvLCFMnNe4Z4CPXzToowvhHvA=="

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    data class PairRequestResult(
        val deviceId: String,
        val authKey: String,
        val timestamp: Long
    )

    fun startPairing(ip: String, pinOrTlsMarker: String, basePath: String?): PairRequestResult {
        val client = clientFor(ip, pinOrTlsMarker)

        val deviceId = randomDeviceId(16)

        val payload = JSONObject().apply {
            put("scope", org.json.JSONArray(listOf("read", "write", "control")))
            put("device", JSONObject().apply {
                put("device_name", "Galaxy Watch")
                put("device_os", "WearOS")
                put("app_id", "de.robnice.philipstvcontrol")
                put("app_name", "PhilipsTVControl")
                put("type", "native")
                put("id", deviceId)
            })
        }

        val url = buildUrl(ip, basePath, "/pair/request")
        val req = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody(jsonType))
            .build()

        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw RuntimeException("pair/request failed: ${resp.code} $body")
            }

            val json = JSONObject(body)
            val authKey = json.getString("auth_key")
            val ts = json.getLong("timestamp")

            return PairRequestResult(deviceId = deviceId, authKey = authKey, timestamp = ts)
        }
    }

    fun grantPairing(
        ip: String,
        pinOrTlsMarker: String,
        basePath: String?,
        request: PairRequestResult,
        userPin: String
    ): Boolean {
        val client = clientFor(ip, pinOrTlsMarker)

        val signature = createAuthSignature(request.timestamp.toString() + userPin)

        val payload = JSONObject().apply {
            put("auth", JSONObject().apply {
                put("pin", userPin)
                put("auth_timestamp", request.timestamp)
                put("auth_signature", signature)
                put("auth_AppId", 1)
            })
            put("device", JSONObject().apply {
                put("device_name", "Galaxy Watch")
                put("device_os", "WearOS")
                put("app_id", "de.robnice.philipstvcontrol")
                put("app_name", "PhilipsTVControl")
                put("type", "native")
                put("id", request.deviceId)
            })
        }

        val url = buildUrl(ip, basePath, "/pair/grant")

        val digestClient = client.newBuilder()
            .authenticator(DigestAuthenticator(request.deviceId, request.authKey))
            .build()

        val req = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody(jsonType))
            .build()

        digestClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            Log.d("TV", "pair/grant -> code=${resp.code} body=${body.take(300)}")

            if (!resp.isSuccessful) return false

            if (body.isNotBlank()) {
                val json = JSONObject(body)
                val errorId = json.optString("error_id", "")
                val errorText = json.optString("error_text", "")

                Log.d("TV", "pair/grant error_id=$errorId, error_text=$errorText")

                if (errorId.isNotBlank()) {
                    return errorId.equals("SUCCESS", ignoreCase = true)
                }
            }

            return true
        }
    }

    private fun clientFor(ip: String, pinOrTlsMarker: String): OkHttpClient {
        return okHttpFactory.unsafeProbeClient()
    }

    private fun buildUrl(ip: String, basePath: String?, suffix: String): String {
        val bp = when {
            basePath.isNullOrBlank() -> "/6"
            basePath.startsWith("/") -> basePath
            else -> "/$basePath"
        }
        return "https://$ip:1926$bp$suffix"
    }

    private fun randomDeviceId(len: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val rnd = SecureRandom()
        return (1..len).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
    }

    private fun createAuthSignature(message: String): String {
        val secret = Base64.decode(secretKeyBase64, Base64.DEFAULT)
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(secret, "HmacSHA1"))
        val raw = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        val hex = raw.joinToString("") { "%02x".format(it) }
        return Base64.encodeToString(hex.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }
}