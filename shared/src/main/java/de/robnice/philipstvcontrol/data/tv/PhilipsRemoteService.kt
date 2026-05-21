package de.robnice.philipstvcontrol.data.tv

import de.robnice.philipstvcontrol.data.net.OkHttpFactory
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.EOFException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException

class PhilipsRemoteService(
    private val okHttpFactory: OkHttpFactory
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val connectionPool = ConnectionPool(
        5,
        5,
        TimeUnit.MINUTES
    )

    private val clientCache = mutableMapOf<String, OkHttpClient>()

    fun sendKey(
        ip: String,
        tlsPinOrMarker: String,
        basePath: String?,
        digestUser: String,
        digestPass: String,
        key: String
    ): Boolean {

        val cacheKey = "$ip|$digestUser|$digestPass"

        val client = clientCache.getOrPut(cacheKey) {

            okHttpFactory
                .unsafeProbeClient()
                .newBuilder()
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .authenticator(DigestAuthenticator(digestUser, digestPass))
                .build()
        }

        val normalizedBasePath = when {
            basePath.isNullOrBlank() -> "/6"
            basePath.startsWith("/") -> basePath
            else -> "/$basePath"
        }

        val url = "https://$ip:1926$normalizedBasePath/input/key"

        val body = """{"key":"$key"}"""
            .toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Connection", "keep-alive")
            .build()

        fun doSend(): Boolean {
            client.newCall(request).execute().use { resp ->
                val bodyStr = resp.body?.string().orEmpty()

                android.util.Log.d(
                    "TV",
                    "input/key -> code=${resp.code} body=${bodyStr.take(120)}"
                )

                return resp.isSuccessful
            }
        }

        return try {
            doSend()
        } catch (e: SSLHandshakeException) {
            android.util.Log.d("TV", "input/key handshake failed, retrying once", e)
            doSend()
        } catch (e: EOFException) {
            android.util.Log.d("TV", "input/key EOF, retrying once", e)
            doSend()
        }
    }
}